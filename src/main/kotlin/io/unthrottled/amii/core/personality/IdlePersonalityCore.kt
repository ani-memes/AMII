package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.MemeLifecycleListener
import io.unthrottled.amii.memes.PanelDismissalOptions
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.gt
import io.unthrottled.amii.tools.lt

class IdlePersonalityCore : PersonalityCore {

  private var reactedEmotion: Mood? = null

  private fun getMoodMapping(mood: Mood?) =
    when (mood) {
      Mood.PATIENT -> 1
      Mood.BORED -> 2
      Mood.TIRED -> 3
      else -> -1
    }

  override fun processUserEvent(
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
          .withComparator { meme ->
            when (meme.userEvent.type) {
              UserEvents.IDLE -> compareMoods(reactedEmotion, mood)
              else -> Comparison.GREATER
            }
          }.build().apply {
            this.addListener(object : MemeLifecycleListener {
              override fun onDisplay() {
                reactedEmotion = mood
              }
            })
          }
      }
  }

  private fun compareMoods(reactedEmotion: Mood?, mood: Mood): Comparison =
    when (getMoodMapping(mood) - getMoodMapping(reactedEmotion)) {
      in lt(0) -> Comparison.LESSER
      in gt(0) -> Comparison.GREATER
      else -> Comparison.EQUAL
    }
}
