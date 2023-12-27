package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import kotlin.random.Random

class EmotionCore(
  config: Config,
  private val random: Random = Random(Random(System.currentTimeMillis()).nextLong())
) {
  companion object {
    private const val MAX_USER_HISTORY = 10
  }

  private val negativeDerivationUnit = NegativeEmotionDerivationUnit(
    config,
    random
  )
  private val positiveDerivationUnit = PositiveEmotionDerivationUnit(
    random
  )
  private val neutralDerivationUnit = NeutralEmotionDerivationUnit(
    config,
    random
  )
  private var emotionalState = EmotionalState(Mood.CALM)

  val currentMood: Mood
    get() = emotionalState.mood

  fun updateConfig(config: Config): EmotionCore =
    EmotionCore(config, random).let {
      it.emotionalState = this.emotionalState
      it
    }

  fun deriveMood(userEvent: UserEvent): Mood =
    deriveAndPersistEmotionalState {
      processEvent(userEvent, emotionalState)
    }

  fun mutateMood(emotionalMutationAction: EmotionalMutationAction): Mood =
    deriveAndPersistEmotionalState {
      processMutation(emotionalMutationAction)
    }

  private fun processEvent(
    userEvent: UserEvent,
    emotionalState: EmotionalState
  ): EmotionalState =
    when (userEvent.category) {
      UserEventCategory.POSITIVE -> positiveDerivationUnit.deriveEmotion(userEvent, emotionalState)
      UserEventCategory.NEGATIVE -> negativeDerivationUnit.deriveEmotion(userEvent, emotionalState)
      UserEventCategory.NEUTRAL -> neutralDerivationUnit.deriveEmotion(userEvent, emotionalState)
    }.copy()
      .apply {
        this.previousEvents.push(userEvent)
        if (this.previousEvents.size > MAX_USER_HISTORY) {
          this.previousEvents.pollLast()
        }
      }

  private fun processMutation(
    emotionalMutationAction: EmotionalMutationAction
  ) = when (emotionalMutationAction.moodCategory) {
    MoodCategory.POSITIVE -> positiveDerivationUnit.deriveFromMutation(emotionalMutationAction, emotionalState)
    MoodCategory.NEGATIVE -> negativeDerivationUnit.deriveFromMutation(emotionalMutationAction, emotionalState)
    MoodCategory.NEUTRAL -> neutralDerivationUnit.deriveFromMutation(emotionalMutationAction, emotionalState)
  }

  private fun deriveAndPersistEmotionalState(stateSupplier: () -> EmotionalState): Mood {
    emotionalState = stateSupplier()
    return emotionalState.mood
  }
}

enum class Mood(val displayValue: String) {
  ENRAGED("enraged"),
  FRUSTRATED("frustrated"),
  AGITATED("agitated"),
  HAPPY("happy"),
  RELIEVED("relieved"),
  EXCITED("excited"),
  PROUD("proud"),
  AMAZED("amazed"),
  SMUG("smug"),
  SHOCKED("shocked"),
  SURPRISED("surprised"),
  CALM("calm"),
  PATIENT("patient"),
  BORED("bored"),
  TIRED("tired"),
  DISAPPOINTED("disappointed"),
  MILDLY_DISAPPOINTED("mildly disappointed"),
  ATTENTIVE("attentive")
}
