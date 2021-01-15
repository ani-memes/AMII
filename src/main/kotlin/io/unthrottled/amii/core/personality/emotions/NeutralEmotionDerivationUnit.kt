package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.gt
import kotlin.random.Random

internal class NeutralEmotionDerivationUnit(
  private val config: Config,
  private val random: Random
) : EmotionDerivationUnit {

  private companion object {
    private const val BOREDOM_THRESHOLD = 3
  }

  override fun deriveEmotion(
    userEvent: UserEvent,
    emotionalState: EmotionalState
  ): EmotionalState =
    when (userEvent.type) {
      UserEvents.IDLE -> processIdleEvent(emotionalState).let {
        it.copy(observedNeutralEvents = it.observedNeutralEvents + 1)
      }
      UserEvents.RETURN -> EmotionalState(Mood.ATTENTIVE)
      else -> emotionalState
    }

  private fun processIdleEvent(emotionalState: EmotionalState): EmotionalState =
    EmotionalState(
      when (emotionalState.observedNeutralEvents) {
        in gt(BOREDOM_THRESHOLD) -> Mood.TIRED
        in 1..BOREDOM_THRESHOLD -> Mood.BORED
        else -> Mood.PATIENT
      },
      observedNeutralEvents = emotionalState.observedNeutralEvents
    )

  override fun deriveFromMutation(
    emotionalMutationAction: EmotionalMutationAction,
    emotionalState: EmotionalState
  ): EmotionalState {
    TODO("Not yet implemented")
  }
}
