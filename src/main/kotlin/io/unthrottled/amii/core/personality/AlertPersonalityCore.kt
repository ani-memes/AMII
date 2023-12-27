package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.MemeEvent
import io.unthrottled.amii.memes.memeEventService

class AlertPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeEventService()
      .createAndDisplayMemeEventFromCategory(
        userEvent,
        MemeAssetCategory.ALERT
      ) {
        MemeEvent(
          meme = it.build(),
          userEvent = userEvent,
          comparator = {
            Comparison.LESSER
          }
        )
      }
  }
}
