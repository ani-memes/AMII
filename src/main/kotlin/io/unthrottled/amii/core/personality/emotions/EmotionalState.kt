package io.unthrottled.amii.core.personality.emotions

import io.unthrottled.amii.events.UserEvent
import java.util.LinkedList

internal data class EmotionalState(
  val mood: Mood,
  val previousEvents: LinkedList<UserEvent> = LinkedList(),
  val observedPositiveEvents: Int = 0,
  val observedNeutralEvents: Int = 0,
  val observedNegativeEvents: Int = 0
)
