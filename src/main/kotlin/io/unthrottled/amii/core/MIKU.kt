package io.unthrottled.amii.core

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.config.Config
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
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.AlarmDebouncer
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger

// Meme Inference Knowledge Unit
class MIKU : UserEventListener, EmotionalMutationActionListener, MoodListener, Disposable, Logging {

  companion object {
    private const val DEBOUNCE_INTERVAL = 80
  }

  private var emotionCore = EmotionCore(Config.instance)
  private val taskPersonalityCore = TaskPersonalityCore()
  private val onDemandPersonalityCore = OnDemandPersonalityCore()
  private val alertPersonalityCore = AlertPersonalityCore()
  private val idlePersonalityCore = IdlePersonalityCore()
  private val greetingPersonalityCore = GreetingPersonalityCore()
  private val resetCore = ResetCore()
  private val singleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL)
  private val idleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL)
  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBusConnection.subscribe(EMOTION_TOPIC, this)
    messageBusConnection.subscribe(EMOTIONAL_MUTATION_TOPIC, this)
  }

  override fun onDispatch(userEvent: UserEvent) {
    if (Config.instance.eventEnabled(userEvent.type).not()) return

    logger().warn("Seen user event $userEvent")
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
    val emotionalState = emotionCore.deriveMood(bufferedUserEvents.first())
    bufferedUserEvents.forEach { userEvent -> reactToEvent(userEvent, emotionalState) }
  }

  private fun consumeEvent(userEvent: UserEvent) {
    val currentMood = emotionCore.deriveMood(userEvent)
    reactToEvent(userEvent, currentMood)
    publishMood(currentMood)
  }

  override fun onAction(emotionalMutationAction: EmotionalMutationAction) {
    val mutatedMood = emotionCore.mutateMood(emotionalMutationAction)
    reactToMutation(emotionalMutationAction)
    publishMood(mutatedMood)
  }

  private fun reactToMutation(
    emotionalMutationAction: EmotionalMutationAction
  ) {
    if (emotionalMutationAction.type == EmotionalMutationType.RESET) {
      resetCore.processMutationEvent(emotionalMutationAction)
    }
  }

  override fun onRequestMood() {
    publishMood(emotionCore.currentMood)
  }

  private fun publishMood(currentMood: Mood) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(EMOTION_TOPIC)
      .onDerivedMood(currentMood)
  }

  private fun reactToEvent(userEvent: UserEvent, emotionalState: Mood) {
    when (userEvent.type) {
      UserEvents.TEST,
      UserEvents.TASK,
      UserEvents.PROCESS -> taskPersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.ON_DEMAND -> onDemandPersonalityCore.processUserEvent(userEvent, emotionalState)
      UserEvents.IDLE -> idlePersonalityCore.processUserEvent(userEvent, emotionalState)
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
  }
}
