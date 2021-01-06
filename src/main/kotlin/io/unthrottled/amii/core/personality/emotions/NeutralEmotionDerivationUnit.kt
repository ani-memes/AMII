package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.gt
import io.unthrottled.amii.tools.lt
import kotlin.random.Random

internal class NeutralEmotionDerivationUnit(
  private val config: Config,
  private val random: Random
) : EmotionDerivationUnit {

  // todo: get bored after sequential idle events
  override fun deriveEmotion(
    userEvent: UserEvent,
    emotionalState: EmotionalState
  ): EmotionalState =
    when (userEvent.type) {
      UserEvents.IDLE -> processIdleEvent(emotionalState)
      else -> emotionalState
    }.copy(
      observedNeutralEvents = emotionalState.observedNeutralEvents + 1
    )

  private fun processIdleEvent(emotionalState: EmotionalState) =
    EmotionalState(
      when (emotionalState.observedNeutralEvents) {
        in gt(3) -> Mood.TIRED
        in 1..3 -> Mood.BORED
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
