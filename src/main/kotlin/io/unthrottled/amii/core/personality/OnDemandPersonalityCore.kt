package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.MemeFactory

class OnDemandPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    MemeFactory.createMeme(
      userEvent.project,
      MemeAssetCategory.MOTIVATION,
    ) {
      it.display()
    }
  }
}
