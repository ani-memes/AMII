package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.MemeService

class OnDemandPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.getService(MemeService::class.java)
      .createMeme(
        userEvent,
        MemeAssetCategory.MOTIVATION,
      ) {
        it.withComparator { 1 }.build()
      }
  }
}
