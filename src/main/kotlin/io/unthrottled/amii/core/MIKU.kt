package io.unthrottled.amii.core

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.core.personality.AlertPersonalityCore
import io.unthrottled.amii.core.personality.GreetingPersonalityCore
import io.unthrottled.amii.core.personality.IdlePersonalityCore
import io.unthrottled.amii.core.personality.OnDemandPersonalityCore
import io.unthrottled.amii.core.personality.ResetCore
import io.unthrottled.amii.core.personality.TaskPersonalityCore
import io.unthrottled.amii.core.personality.emotions.EMOTIONAL_MUTATION_TOPIC
import io.unthrottled.amii.core.personality.emotions.EMOTION_TOPIC
import io.unthrottled.amii.core.personality.emotions.EmotionCore
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationAction
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationActionListener
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationType
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.core.personality.emotions.MoodListener
import io.unthrottled.amii.discreet.discreetModeService
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.AlarmDebouncer
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafely

// Meme Inference Knowledge Unit
class MIKU(private val project: Project) :
  UserEventListener,
  EmotionalMutationActionListener,
  MoodListener,
  Disposable,
  Logging {

  companion object {
    private const val DEBOUNCE_INTERVAL = 80
    val USER_TRIGGERED_EVENTS = setOf(
      UserEvents.TEST,
      UserEvents.TASK,
      UserEvents.PROCESS,
    )
  }

  private var emotionCore = EmotionCore(Config.instance)
  private var modalityProcessor = ModalityProcessor(Config.instance, project)
  private val taskPersonalityCore = TaskPersonalityCore()
  private val onDemandPersonalityCore = OnDemandPersonalityCore()
  private val alertPersonalityCore = AlertPersonalityCore()
  private val idlePersonalityCore = IdlePersonalityCore(project)
  private val greetingPersonalityCore = GreetingPersonalityCore()
  private val resetCore = ResetCore()
  private val singleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL)
  private val idleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL)
  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
  private val projectMessageBusConnection = project.messageBus.connect()

  init {
    ApplicationManager.getApplication().invokeLater {
      attemptToSubscribe { projectMessageBusConnection.subscribe(EMOTION_TOPIC, this) }
      attemptToSubscribe { projectMessageBusConnection.subscribe(EMOTIONAL_MUTATION_TOPIC, this) }
      attemptToSubscribe {
        messageBusConnection.subscribe(
          CONFIG_TOPIC,
          ConfigListener {
            emotionCore = emotionCore.updateConfig(it)
            modalityProcessor = modalityProcessor.updateConfig(it)
          }
        )
      }
    }
  }

  private fun attemptToSubscribe(subscribingFunction: () -> Unit) {
    runSafely(subscribingFunction) {
      logger().warn("Unable to subscribe for reasons", it)
      runSafely(subscribingFunction) {
        logger().warn("Second subscription attempt failed", it)
        UpdateNotification.sendMessage(
          PluginMessageBundle.message("miku.startup.error.title"),
          PluginMessageBundle.message("miku.startup.error.body"),
        )
      }
    }
  }

  override fun onDispatch(userEvent: UserEvent) {
    logger().debug("Seen user event $userEvent")
    if (Config.instance.eventEnabled(userEvent.type).not()) return

    logger().debug("Attempting to consume user event $userEvent")
    when (userEvent.type) {
      UserEvents.IDLE ->
        idleEventDebouncer.debounceAndBuffer(userEvent) {
          consumeEvents(it)
        }
      else -> singleEventDebouncer.debounce {
        consumeEvent(userEvent)
      }
    }
  }

  private fun consumeEvents(bufferedUserEvents: List<UserEvent>) {
    if(modalityProcessor.shouldProcess(bufferedUserEvents).not()) return

    val emotionalState = emotionCore.deriveMood(bufferedUserEvents.first())
    bufferedUserEvents.forEach { userEvent -> reactToEvent(userEvent, emotionalState) }
    publishMood(emotionalState)
  }

  private fun consumeEvent(userEvent: UserEvent) {
    if (modalityProcessor.shouldProcess(userEvent).not()) return

    val currentMood = emotionCore.deriveMood(userEvent)
    reactToEvent(userEvent, currentMood)
    publishMood(currentMood)
  }

  override fun onAction(emotionalMutationAction: EmotionalMutationAction) {
    if (emotionalMutationAction.project?.isDisposed == true) return

    val mutatedMood = emotionCore.mutateMood(emotionalMutationAction)
    reactToMutation(emotionalMutationAction)
    publishMood(mutatedMood)
  }

  private fun reactToMutation(
    emotionalMutationAction: EmotionalMutationAction
  ) {
    if(project.discreetModeService().isDiscreetMode) return

    if (emotionalMutationAction.type == EmotionalMutationType.RESET) {
      resetCore.processMutationEvent(emotionalMutationAction)
    }
  }

  override fun onRequestMood() {
    publishMood(emotionCore.currentMood)
  }

  private fun publishMood(currentMood: Mood) {
    if (project.isDisposed) return

    project.messageBus
      .syncPublisher(EMOTION_TOPIC)
      .onDerivedMood(currentMood)
  }

  private fun reactToEvent(userEvent: UserEvent, emotionalState: Mood) {
    when (userEvent.type) {
      in USER_TRIGGERED_EVENTS -> taskPersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.SILENCE,
      UserEvents.ON_DEMAND -> onDemandPersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.IDLE,
      UserEvents.RETURN,
      -> idlePersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.LOGS -> alertPersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.STARTUP -> greetingPersonalityCore.processUserEvent(userEvent, emotionalState)
      else -> {
      }
    }
  }

  override fun dispose() {
    singleEventDebouncer.dispose()
    idleEventDebouncer.dispose()
    messageBusConnection.dispose()
    projectMessageBusConnection.dispose()
  }
}
