package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.MemeEvent
import io.unthrottled.amii.memes.PanelDismissalOptions
import io.unthrottled.amii.memes.memeEventService

class GreetingPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeEventService()
      .createAndDisplayMemeEventFromCategory(
        userEvent,
        MemeAssetCategory.WELCOMING,
      ) {
        it
          .withDismissalMode(PanelDismissalOptions.TIMED)
          .build().let { meme ->
            MemeEvent(
              meme = meme,
              userEvent = userEvent,
              comparator = {
                Comparison.GREATER
              }
            )
          }
      }
  }
}
