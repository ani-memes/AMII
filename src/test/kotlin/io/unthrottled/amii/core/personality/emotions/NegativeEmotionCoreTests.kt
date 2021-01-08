package io.unthrottled.amii.core.personality.emotions

import com.intellij.openapi.project.Project
import io.mockk.every
import io.mockk.mockk
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.core.personality.emotions.NegativeEmotionDerivationUnit.Companion.OTHER_NEGATIVE_EMOTIONS
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.toList
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.random.Random

class NegativeEmotionCoreTests {

  @Test
  fun deriveMoodShouldReturnCalmAfterIdleEvent() {
    val emotionCore = EmotionCore(
      Config()
    )

    listOf(
      buildUserEvent(
        UserEvents.IDLE,
        UserEventCategory.NEUTRAL
      ),

      buildUserEvent(
        UserEvents.IDLE,
        UserEventCategory.NEGATIVE
      ),

      buildUserEvent(
        UserEvents.IDLE,
        UserEventCategory.POSITIVE
      )
    ).forEachIndexed { index, event ->
      val deriveMood = emotionCore.deriveMood(
        event
      )
      val expectedMood = Mood.PATIENT
      Assertions.assertThat(
        deriveMood
      ).withFailMessage(
        """At index #$index
                    |$event
                    |did not create $expectedMood but did $deriveMood
                """.trimMargin()
      ).isEqualTo(expectedMood)
    }
  }

  @Test
  fun deriveMoodShouldAlwaysReturnFrustratedAfterExpectedEvents() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 4
        probabilityOfFrustration = 100
      }
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS
    val frustrated = Mood.FRUSTRATED.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated
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

  @Test
  fun shouldCalmDownAfterIdleEvent() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      }
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS
    val calm = Mood.PATIENT.toList()
    val frustrated = Mood.FRUSTRATED.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.IDLE,
        UserEventCategory.NEUTRAL
      ) to calm,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED)
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

  @Test
  fun `should never return frustration when frustration is disabled`() {
    val emotionCore = EmotionCore(
      Config().apply {
        allowFrustration = false
        eventsBeforeFrustration = 0
        probabilityOfFrustration = 100
      }
    )

    val motivationEvent = buildUserEvent(
      UserEvents.TASK,
      UserEventCategory.NEGATIVE
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS.toTypedArray()
    repeat(42) { index ->
      val deriveMood = emotionCore.deriveMood(
        motivationEvent
      )
      Assertions.assertThat(
        deriveMood
      ).withFailMessage(
        """At index #$index
                    |$motivationEvent
                    |did not create $OTHER_NEGATIVE_EMOTIONS but did $deriveMood
                """.trimMargin()
      ).isIn(
        *negativeEmotions
      )
    }
  }

  @Test
  fun deriveMoodShouldReturnNeverReturnFrustratedWhenProbabilityIsZero() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 0
        probabilityOfFrustration = 0
      }
    )

    val motivationEvent = buildUserEvent(
      UserEvents.TASK,
      UserEventCategory.NEGATIVE
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS.toTypedArray()
    repeat(42) { index ->
      val deriveMood = emotionCore.deriveMood(
        motivationEvent
      )
      Assertions.assertThat(
        deriveMood
      ).withFailMessage(
        """At index #$index
                    |$motivationEvent
                    |did not create $OTHER_NEGATIVE_EMOTIONS but did $deriveMood
                """.trimMargin()
      ).isIn(
        *negativeEmotions
      )
    }
  }

  @Test
  fun `frustration should evolve into rage`() {
    val mockRandom = mockk<Random>()
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      },
      mockRandom
    )

    every { mockRandom.nextLong(1, 100) } returns 50
    every { mockRandom.nextInt(2) } returns 1

    val frustrated = Mood.FRUSTRATED.toList()
    val enraged = Mood.ENRAGED.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to OTHER_NEGATIVE_EMOTIONS[1].toList(),
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to enraged,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to enraged,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to enraged
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

  @Test
  fun `frustration should cool down when positive events happen`() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      }
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS
    val frustrated = Mood.FRUSTRATED.toList()
    val relieved = Mood.RELIEVED.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.POSITIVE
      ) to relieved,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.POSITIVE
      ) to listOf(Mood.HAPPY, Mood.EXCITED),
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.POSITIVE
      ) to relieved,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED)
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

  @Test
  fun `frustration should cool down when cool down mutation events happen`() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      }
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS
    val frustrated = Mood.FRUSTRATED.toList()
    val calm = Mood.CALM.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to calm,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to calm
    ).forEachIndexed { index, arguments ->
      val deriveMood = when (val input = arguments.first) {
        is UserEvent -> emotionCore.deriveMood(input)
        is EmotionalMutationAction -> emotionCore.mutateMood(input)
        else -> throw NotImplementedError("Test not configured for $input")
      }
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

  @Test
  fun `frustration should reset  when reset mutation events happen`() {
    val emotionCore = EmotionCore(
      Config().apply {
        eventsBeforeFrustration = 1
        probabilityOfFrustration = 100
      }
    )

    val negativeEmotions =
      OTHER_NEGATIVE_EMOTIONS
    val frustrated = Mood.FRUSTRATED.toList()
    val calm = Mood.CALM.toList()
    listOf(
      buildUserEvent(
        UserEvents.TASK,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.RESET,
        MoodCategory.NEGATIVE
      ) to calm,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to negativeEmotions,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      EmotionalMutationAction(
        EmotionalMutationType.COOL_DOWN,
        MoodCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to frustrated,
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      buildUserEvent(
        UserEvents.TEST,
        UserEventCategory.NEGATIVE
      ) to listOf(Mood.FRUSTRATED, Mood.ENRAGED),
      EmotionalMutationAction(
        EmotionalMutationType.RESET,
        MoodCategory.NEGATIVE
      ) to calm
    ).forEachIndexed { index, arguments ->
      val deriveMood = when (val input = arguments.first) {
        is UserEvent -> emotionCore.deriveMood(input)
        is EmotionalMutationAction -> emotionCore.mutateMood(input)
        else -> throw NotImplementedError("Test not configured for $input")
      }
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

private val projectMock = mockk<Project>()

internal fun buildUserEvent(
  type: UserEvents,
  category: UserEventCategory
): UserEvent {
  return UserEvent(
    type,
    category,
    "眼睛看不见我",
    projectMock
  )
}
