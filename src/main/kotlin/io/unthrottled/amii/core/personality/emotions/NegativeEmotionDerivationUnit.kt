package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.ProbabilityTools
import io.unthrottled.amii.tools.toStream
import java.lang.Integer.max
import java.util.stream.Stream
import kotlin.random.Random

@Suppress("TooManyFunctions")
internal class NegativeEmotionDerivationUnit(
  private val config: Config,
  private val random: Random
) : EmotionDerivationUnit {

  companion object {
    val OTHER_NEGATIVE_EMOTIONS = listOf(
      Mood.SHOCKED,
      Mood.DISAPPOINTED
    )
    private const val FRUSTRATION_WEIGHT = 0.75
  }

  private val probabilityTools = ProbabilityTools(random)

  override fun deriveEmotion(
    userEvent: UserEvent,
    emotionalState: EmotionalState
  ): EmotionalState =
    when {
      shouldProcessNegativeEvent(userEvent) -> processNegativeEvent(emotionalState)
      else -> emotionalState
    }

  override fun deriveFromMutation(
    emotionalMutationAction: EmotionalMutationAction,
    emotionalState: EmotionalState
  ): EmotionalState =
    when (emotionalMutationAction.type) {
      EmotionalMutationType.COOL_DOWN -> coolDown(emotionalState)
      EmotionalMutationType.RESET -> takeAChillPill(emotionalState)
    }

  private fun takeAChillPill(emotionalState: EmotionalState): EmotionalState =
    emotionalState.copy(
      mood = Mood.CALM,
      observedNegativeEvents = 0
    )

  private fun coolDown(emotionalState: EmotionalState): EmotionalState {
    val observedNegativeEvents = emotionalState.observedNegativeEvents
    val cooledDownNegativeEvents = max(0, observedNegativeEvents - 1)
    return emotionalState.copy(
      mood =
      if (hasCalmedDown(cooledDownNegativeEvents)) Mood.CALM
      else pickNegativeMood(cooledDownNegativeEvents),
      observedNegativeEvents = cooledDownNegativeEvents
    )
  }

  private fun shouldProcessNegativeEvent(userEvent: UserEvent): Boolean =
    userEvent.type != UserEvents.IDLE

  private fun processNegativeEvent(emotionalState: EmotionalState): EmotionalState {
    val observedFrustrationEvents = emotionalState.observedNegativeEvents
    return emotionalState.copy(
      mood = pickNegativeMood(observedFrustrationEvents),
      observedNegativeEvents = observedFrustrationEvents + 1
    )
  }

  private fun pickNegativeMood(observedFrustrationEvents: Int) =
    when {
      shouldBeEnraged(observedFrustrationEvents) ->
        hurryFindCover()

      shouldBeFrustrated(observedFrustrationEvents) ->
        tryToRemainCalm()

      // todo: more appropriate choice based off of previous state
      else -> OTHER_NEGATIVE_EMOTIONS.random(random)
    }

  private fun shouldBeFrustrated(observedFrustrationEvents: Int) =
    config.allowFrustration &&
      config.eventsBeforeFrustration <= observedFrustrationEvents

  private fun hasCalmedDown(observedFrustrationEvents: Int) =
    config.eventsBeforeFrustration / 2 >= observedFrustrationEvents

  private fun shouldBeEnraged(observedFrustrationEvents: Int) =
    shouldBeFrustrated(observedFrustrationEvents) &&
      observedFrustrationEvents >= config.eventsBeforeFrustration * 2

  private fun hurryFindCover(): Mood {
    val rageProbability = (config.probabilityOfFrustration * (FRUSTRATION_WEIGHT)).toLong()
    val primaryEmotions =
      Stream.of(
        Mood.ENRAGED to rageProbability,
        Mood.FRUSTRATED to config.probabilityOfFrustration - rageProbability
      )

    return pickNegativeEmotion(primaryEmotions)
  }

  private fun tryToRemainCalm(): Mood {
    val primaryEmotions =
      (Mood.FRUSTRATED to config.probabilityOfFrustration.toLong())
        .toStream()
    return pickNegativeEmotion(primaryEmotions)
  }

  private fun pickNegativeEmotion(
    primaryEmotions: Stream<Pair<Mood, Long>>
  ): Mood {
    val secondaryEmotions = OTHER_NEGATIVE_EMOTIONS
    val probabilityOfPrimaryEmotions = config.probabilityOfFrustration.toLong()
    return probabilityTools.pickEmotion(probabilityOfPrimaryEmotions, primaryEmotions, secondaryEmotions)
  }
}
