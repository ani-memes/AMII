package io.unthrottled.amii.core.personality

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.MemeLifecycleListener
import io.unthrottled.amii.memes.PanelDismissalOptions
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.gt
import io.unthrottled.amii.tools.lt

class IdlePersonalityCore : PersonalityCore, Logging {

  companion object {
    private const val MOOD_KEY = "mood"
  }

  @Suppress("MagicNumber")
  private fun getMoodMapping(mood: Mood?) =
    when (mood) {
      Mood.PATIENT -> 1
      Mood.BORED -> 2
      Mood.TIRED -> 3
      else -> 0
    }

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    if (userEvent.type == UserEvents.IDLE) reactToIdleEvent(userEvent, mood)
  }

  private fun reactToIdleEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeService()
      .createMeme(
        userEvent,
        when (mood) {
          Mood.BORED -> MemeAssetCategory.BORED
          Mood.TIRED -> MemeAssetCategory.TIRED
          else -> MemeAssetCategory.PATIENTLY_WAITING
        },
      ) {
        it.withDismissalMode(PanelDismissalOptions.FOCUS_LOSS)
          .withAnchor(Config.instance.idleNotificationAnchor)
          .withMetaData(
            mapOf(
              MOOD_KEY to mood
            )
          )
          .withComparator { currentDisplayedMeme ->
            when (currentDisplayedMeme.userEvent.type) {
              UserEvents.IDLE -> compareMoods(mood, currentDisplayedMeme.metadata[MOOD_KEY] as? Mood)
              else -> Comparison.GREATER
            }
          }.build().apply {
            this.addListener(
              object : MemeLifecycleListener {
                override fun onDismiss() {
                  ApplicationManager.getApplication().messageBus
                    .syncPublisher(EVENT_TOPIC)
                    .onDispatch(
                      UserEvent(
                        UserEvents.RETURN,
                        UserEventCategory.NEUTRAL,
                        PluginMessageBundle.message("user.event.idle.name"),
                        userEvent.project
                      )
                    )
                }
              }
            )
          }
      }
  }

  private fun compareMoods(reactedEmotion: Mood, otherMood: Mood?): Comparison =
    when (getMoodMapping(otherMood) - getMoodMapping(reactedEmotion)) {
      in lt(0) -> Comparison.LESSER
      in gt(0) -> Comparison.GREATER
      else -> Comparison.EQUAL
    }
}
