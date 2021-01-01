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
    private const val TOTAL_WEIGHT = 100
  }

  fun pickEmotion(
    probabilityOfPrimaryEmotions: Int,
    primaryEmotions: Stream<Pair<Mood, Int>>,
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
      random.nextInt(1, TOTAL_WEIGHT),
      weightedEmotions
    ).orElseGet { weightedEmotions.first().first }
  }

  fun <T> pickFromWeightedList(weightedList: List<Pair<T, Int>>): Optional<T> {
    val totalWeight = weightedList.map { it.second }.sum()
    return pickFromWeightedList(
      random.nextInt(1, totalWeight + 1),
      weightedList
    )
  }

  private fun buildWeightedList(
    weightRemaining: Int,
    primaryEmotions: Stream<Pair<Mood, Int>>,
    secondaryEmotions: List<Mood>
  ): List<Pair<Mood, Int>> {
    val secondaryEmotionWeights = weightRemaining / secondaryEmotions.size
    return concat(
      primaryEmotions,
      secondaryEmotions.stream().map { it to secondaryEmotionWeights }
    ).collect(Collectors.toList())
      .shuffled<Pair<Mood, Int>>()
  }

  private fun <T> pickFromWeightedList(
    weightChosen: Int,
    weightedEmotions: List<Pair<T, Int>>
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
