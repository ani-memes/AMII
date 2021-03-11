package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.ProbabilityTools
import java.util.stream.Stream
import kotlin.random.Random

internal class PositiveEmotionDerivationUnit(
  random: Random
) : EmotionDerivationUnit {

  companion object {
    val OTHER_POSITIVE_EMOTIONS = listOf(Mood.HAPPY)
    private const val primaryEmotionProbability = 80L
    private const val excitedProbability = 20L
    private const val standardExcitedProbability = 75L
  }
  private val probabilityTools = ProbabilityTools(random)

  override fun deriveEmotion(
    userEvent: UserEvent,
    emotionalState: EmotionalState
  ): EmotionalState {
    return when {
      shouldProcessPositiveEvent(userEvent) ->
        processPositiveEvent(emotionalState)
      else -> emotionalState
    }
  }

  override fun deriveFromMutation(
    emotionalMutationAction: EmotionalMutationAction,
    emotionalState: EmotionalState
  ): EmotionalState {
    TODO("Not yet implemented")
  }

  private fun shouldProcessPositiveEvent(userEvent: UserEvent): Boolean {
    return userEvent.type != UserEvents.IDLE
  }

  private fun processPositiveEvent(emotionalState: EmotionalState): EmotionalState {
    return emotionalState.copy(
      mood = getPositiveMood(emotionalState),
      observedPositiveEvents = emotionalState.observedPositiveEvents + 1,
      observedNegativeEvents = coolDownFrustration(emotionalState)
    )
  }

  private fun getPositiveMood(
    emotionalState: EmotionalState
  ): Mood {
    return when {
      emotionalState.mood == Mood.FRUSTRATED -> Mood.RELIEVED
      emotionalState.previousEvent?.category == UserEventCategory.NEGATIVE ->
        deriveProblemSolvedMood()
      else -> deriveStandardPositiveMood()
    }
  }

  private fun deriveStandardPositiveMood(): Mood {
    val primaryEmotions =
      Stream.of(
        Mood.EXCITED to standardExcitedProbability
      )
    return probabilityTools.pickEmotion(
      standardExcitedProbability,
      primaryEmotions,
      OTHER_POSITIVE_EMOTIONS
    )
  }

  private fun deriveProblemSolvedMood(): Mood {
    val primaryEmotions =
      Stream.of(
        Mood.SMUG to primaryEmotionProbability - excitedProbability,
        Mood.EXCITED to excitedProbability,
      )
    return probabilityTools.pickEmotion(
      primaryEmotionProbability,
      primaryEmotions,
      OTHER_POSITIVE_EMOTIONS
    )
  }

  private fun coolDownFrustration(emotionalState: EmotionalState): Int =
    if (emotionalState.observedNegativeEvents > 0) {
      emotionalState.observedNegativeEvents - 1
    } else {
      0
    }
}
