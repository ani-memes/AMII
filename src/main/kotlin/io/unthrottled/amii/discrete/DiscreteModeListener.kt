package io.unthrottled.amii.discrete

import com.intellij.util.messages.Topic
import java.util.EventListener

enum class DiscreteMode {
  ACTIVE, INACTIVE
}

fun Boolean.toDiscreteMode(): DiscreteMode =
  if (this) DiscreteMode.ACTIVE else DiscreteMode.INACTIVE

fun interface DiscreteModeListener : EventListener {
  companion object {
    val DISCRETE_MODE_TOPIC: Topic<DiscreteModeListener> =
      Topic(DiscreteModeListener::class.java)
  }

  fun modeChanged(discreteMode: DiscreteMode)
}
