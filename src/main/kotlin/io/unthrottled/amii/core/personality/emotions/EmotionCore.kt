package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import kotlin.random.Random

class EmotionCore(
  config: Config,
  private val random: Random = Random(Random(System.currentTimeMillis()).nextLong())
) {
  private val negativeDerivationUnit = NegativeEmotionDerivationUnit(
    config,
    random
  )
  private val positiveDerivationUnit = PositiveEmotionDerivationUnit(
    config,
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
    }.copy(
      previousEvent = userEvent
    )

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

enum class Mood {
  ENRAGED,
  FRUSTRATED,
  AGITATED,
  HAPPY,
  RELIEVED,
  EXCITED,
  PROUD,
  AMAZED,
  SMUG,
  SHOCKED,
  SURPRISED,
  CALM,
  PATIENT,
  BORED,
  TIRED,
  DISAPPOINTED,
}
