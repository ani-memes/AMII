package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.memeService

class AlertPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeService()
      .createMeme(
        userEvent,
        MemeAssetCategory.MOTIVATION, // todo: new category
      ) {
        it.withComparator {
          Comparison.LESSER
        }.build()
      }
  }
}
