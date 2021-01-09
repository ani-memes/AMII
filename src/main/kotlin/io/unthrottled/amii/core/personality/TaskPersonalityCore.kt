package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.MIKU.Companion.USER_TRIGGERED_EVENTS
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.memes.Comparison
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.toArray

class TaskPersonalityCore : PersonalityCore {

  override fun processUserEvent(
    userEvent: UserEvent,
    mood: Mood
  ) {
    userEvent.project.memeService()
      .createMemeFromCategories(userEvent, *getRelevantCategories(userEvent, mood)) {
        it
          .withComparator { otherMeme ->
            when (otherMeme.userEvent.type) {
              in USER_TRIGGERED_EVENTS ->
                if (otherMeme.userEvent.category == userEvent.category) Comparison.EQUAL
                else Comparison.GREATER
              else -> Comparison.EQUAL
            }
          }.build()
      }
  }

  private fun getRelevantCategories(
    motivationEvent: UserEvent,
    mood: Mood
  ): Array<out MemeAssetCategory> =
    when (motivationEvent.category) {
      UserEventCategory.POSITIVE -> getPositiveMotivationAsset(mood)
      UserEventCategory.NEGATIVE -> getNegativeMotivationAsset(mood)
      UserEventCategory.NEUTRAL -> arrayOf()
    }

  private fun getPositiveMotivationAsset(mood: Mood): Array<MemeAssetCategory> =
    when (mood) {
      Mood.SMUG -> MemeAssetCategory.SMUG.toArray()
      Mood.HAPPY -> arrayOf(
        MemeAssetCategory.CELEBRATION,
        MemeAssetCategory.HAPPY
      )
      else -> MemeAssetCategory.CELEBRATION.toArray()
    }

  private fun getNegativeMotivationAsset(mood: Mood): Array<MemeAssetCategory> =
    when (mood) {
      Mood.FRUSTRATED -> MemeAssetCategory.FRUSTRATION.toArray()
      Mood.ENRAGED -> MemeAssetCategory.ENRAGED.toArray()
      Mood.SHOCKED -> arrayOf(MemeAssetCategory.SHOCKED)
      Mood.DISAPPOINTED -> arrayOf(
        MemeAssetCategory.DISAPPOINTMENT,
        MemeAssetCategory.DISAPPOINTMENT,
        MemeAssetCategory.MOCKING
      )
      else -> arrayOf(
        MemeAssetCategory.SHOCKED
      )
    }
}
