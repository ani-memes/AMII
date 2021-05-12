package io.unthrottled.amii.core.personality.emotions

import io.mockk.every
import io.mockk.mockk
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.toList
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.random.Random

class PositiveEmotionCoreTests {

  @Test
  fun `should return smug after negative event`() {
    val mockRandom = mockk<Random>()
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      },
      mockRandom
    )

    every { mockRandom.nextLong(1, 100) } returns 50
    every { mockRandom.nextLong(1, 555) } returns 50
    every { mockRandom.nextInt(2) } returns 1
    every { mockRandom.nextInt(3) } returns 1

    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to NegativeEmotionDerivationUnit.OTHER_NEGATIVE_EMOTIONS[0].toList(),
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.POSITIVE
      ) to Mood.SMUG.toList()
    ).forEachIndexed { index, arguments ->
      val deriveMood = emotionCore.deriveMood(
        arguments.first
      )
      Assertions.assertThat(
        deriveMood
      ).withFailMessage(
        """At index #$index
                    |${arguments.first}
                    |did not create ${arguments.second} but did $deriveMood
                """.trimMargin()
      ).isIn(arguments.second)
    }
  }
}
