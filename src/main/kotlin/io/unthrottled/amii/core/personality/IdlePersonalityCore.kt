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

class IdlePersonalityCore : PersonalityCore {

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
          else -> MemeAssetCategory.WAITING
        },
      ) {
        it.withDismissalMode(PanelDismissalOptions.FOCUS_LOSS)
          .withAnchor(Config.instance.idleNotificationAnchor)
          .withComparator { meme ->
            when (meme.userEvent.type) {
              UserEvents.IDLE -> Comparison.EQUAL
              else -> Comparison.GREATER
            }
          }.build().apply {
            this.addListener(object : MemeLifecycleListener {
              override fun onDisplay() {

              }
            })
          }
      }
  }
}
