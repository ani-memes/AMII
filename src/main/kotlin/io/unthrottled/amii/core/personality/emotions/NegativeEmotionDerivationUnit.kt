package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.ProbabilityTools
import io.unthrottled.amii.tools.toStream
import java.util.stream.Stream
import kotlin.math.log2
import kotlin.math.max
import kotlin.random.Random

@Suppress("TooManyFunctions")
internal class NegativeEmotionDerivationUnit(
  private val config: Config,
  private val random: Random
) : EmotionDerivationUnit {

  companion object {
    val OTHER_NEGATIVE_EMOTIONS = listOf(
      Mood.SHOCKED,
      Mood.DISAPPOINTED,
      Mood.MILDLY_DISAPPOINTED
    )
    private const val FRUSTRATION_WEIGHT = 0.75
    private const val TOTAL_NEGATIVE_EMOTION_WEIGHT = 500L

    private val reactableEvents = setOf(
      UserEvents.TEST,
      UserEvents.TASK,
      UserEvents.PROCESS
    )
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
      mood = if (hasCalmedDown(cooledDownNegativeEvents)) {
        Mood.CALM
      } else {
        pickNegativeMood(cooledDownNegativeEvents, emotionalState)
      },
      observedNegativeEvents = cooledDownNegativeEvents
    )
  }

  private fun shouldProcessNegativeEvent(userEvent: UserEvent): Boolean =
    userEvent.type != UserEvents.IDLE

  private fun processNegativeEvent(emotionalState: EmotionalState): EmotionalState {
    val observedFrustrationEvents = emotionalState.observedNegativeEvents
    return emotionalState.copy(
      mood = pickNegativeMood(observedFrustrationEvents, emotionalState),
      observedNegativeEvents = observedFrustrationEvents + 1
    )
  }

  private fun pickNegativeMood(observedFrustrationEvents: Int, emotionalState: EmotionalState) =
    when {
      shouldBeEnraged(observedFrustrationEvents) ->
        hurryFindCover()

      shouldBeFrustrated(observedFrustrationEvents) ->
        tryToRemainCalm()

      else -> pickNextNegativeMood(emotionalState)
    }

  private val chillEvents = setOf(
    UserEventCategory.POSITIVE,
    UserEventCategory.NEUTRAL
  )

  @Suppress("MagicNumber") // because they are magic
  private fun pickNextNegativeMood(
    emotionalState: EmotionalState
  ): Mood {
    val shortTermHistory =
      emotionalState.previousEvents
        .filter { reactableEvents.contains(it.type) }
        .map { it.category }

    val weightedList = when (shortTermHistory.firstOrNull()) {
      in chillEvents, null -> {
        val aThirdOfWeight = TOTAL_NEGATIVE_EMOTION_WEIGHT / 3
        listOf(
          Mood.SHOCKED to TOTAL_NEGATIVE_EMOTION_WEIGHT - aThirdOfWeight,
          Mood.DISAPPOINTED to aThirdOfWeight,
          Mood.MILDLY_DISAPPOINTED to aThirdOfWeight / 3
        )
      }
      UserEventCategory.NEGATIVE -> {
        val comboLength = getComboLength(shortTermHistory)
        val disappointedWeight = (TOTAL_NEGATIVE_EMOTION_WEIGHT / log2(comboLength.toFloat())).toLong()
        listOf(
          Mood.MILDLY_DISAPPOINTED to TOTAL_NEGATIVE_EMOTION_WEIGHT - disappointedWeight,
          Mood.DISAPPOINTED to disappointedWeight
        )
      }
      else ->
        OTHER_NEGATIVE_EMOTIONS
          .map { it to (TOTAL_NEGATIVE_EMOTION_WEIGHT / OTHER_NEGATIVE_EMOTIONS.size) }
    }

    return probabilityTools.pickFromWeightedList(weightedList).orElse(
      OTHER_NEGATIVE_EMOTIONS.random(random)
    )
  }

  private fun getComboLength(shortTermHistory: List<UserEventCategory>): Int {
    data class Accum(
      var comboValue: Int = 1,
      var isCombo: Boolean = true
    )
    return shortTermHistory
      .fold(Accum()) { acc, category ->
        if (acc.isCombo) {
          acc.apply {
            isCombo = category == UserEventCategory.NEGATIVE
            if (isCombo) comboValue++
          }
        } else {
          acc
        }
      }.comboValue
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
