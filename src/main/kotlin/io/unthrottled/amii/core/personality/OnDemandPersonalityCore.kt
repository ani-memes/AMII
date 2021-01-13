package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.memeService

class OnDemandPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeService()
      .createMemeFromCategories(
        userEvent,
        MemeAssetCategory.HAPPY,
        MemeAssetCategory.CELEBRATION,
        MemeAssetCategory.ALERT,
      ) {
        it
          .withSound(
            if (userEvent.type == UserEvents.SILENCE) false
            else Config.instance.soundEnabled
          )
          .withComparator {
            Comparison.GREATER
          }.build()
      }
  }
}
