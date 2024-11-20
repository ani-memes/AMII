package io.unthrottled.amii.tools

import com.intellij.util.containers.concat
import io.unthrottled.amii.core.personality.emotions.Mood
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.random.Random

class ProbabilityTools(
  private val random: Random
) {
  companion object {
    private const val TOTAL_WEIGHT = 100L
  }

  fun pickEmotion(
    probabilityOfPrimaryEmotions: Long,
    primaryEmotions: Stream<Pair<Mood, Long>>,
    secondaryEmotions: List<Mood>
  ): Mood {
    assert(probabilityOfPrimaryEmotions in 0..TOTAL_WEIGHT) { "Expected probability to be from 0 to 100" }
    val weightRemaining = TOTAL_WEIGHT - probabilityOfPrimaryEmotions
    val weightedEmotions = buildWeightedList(
      weightRemaining,
      primaryEmotions,
      secondaryEmotions
    )
    return pickFromWeightedList(
      random.nextLong(1, TOTAL_WEIGHT),
      weightedEmotions
    ).orElseGet { weightedEmotions.first().first }
  }

  fun <T : Any> pickFromWeightedList(weightedList: List<Pair<T, Long>>): Optional<T> {
    val totalWeight = weightedList.sumOf { it.second }
    return pickFromWeightedList(
      random.nextLong(1, if (totalWeight <= 1) 2 else totalWeight),
      weightedList
    )
  }

  private fun buildWeightedList(
    weightRemaining: Long,
    primaryEmotions: Stream<Pair<Mood, Long>>,
    secondaryEmotions: List<Mood>
  ): List<Pair<Mood, Long>> {
    val secondaryEmotionWeights = weightRemaining / secondaryEmotions.size
    return concat(
      primaryEmotions,
      secondaryEmotions.stream().map { it to secondaryEmotionWeights }
    ).collect(Collectors.toList())
      .shuffled<Pair<Mood, Long>>()
  }

  private fun <T : Any> pickFromWeightedList(
    weightChosen: Long,
    weightedEmotions: List<Pair<T, Long>>
  ): Optional<T> {
    var randomWeight = weightChosen
    for ((mood, weight) in weightedEmotions) {
      if (randomWeight <= weight) {
        return mood.toOptional()
      }
      randomWeight -= weight
    }

    return weightedEmotions.first { it.second > 0 }.toOptional()
      .map { it.first }
  }
}
