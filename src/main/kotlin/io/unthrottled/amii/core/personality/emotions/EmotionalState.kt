package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.events.UserEvent

internal data class EmotionalState(
  val mood: Mood,
  val previousEvent: UserEvent? = null,
  val observedPositiveEvents: Int = 0,
  val observedNeutralEvents: Int = 0,
  val observedNegativeEvents: Int = 0
)
