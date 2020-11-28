package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents
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
      UserEvents.IDLE -> EmotionalState(Mood.CALM)
      else -> emotionalState
    }.copy(
      observedNeutralEvents = emotionalState.observedNeutralEvents + 1
    )

  override fun deriveFromMutation(
    emotionalMutationAction: EmotionalMutationAction,
    emotionalState: EmotionalState
  ): EmotionalState {
    TODO("Not yet implemented")
  }
}
