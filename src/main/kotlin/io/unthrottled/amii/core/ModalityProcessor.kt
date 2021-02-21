package io.unthrottled.amii.core

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent

class ModalityProcessor(private val config: Config) {

  private var lastSeenUserEvent: UserEvent? = null

  fun shouldProcess(userEvent: UserEvent): Boolean {
    val shouldReact = shouldReact(userEvent)
    lastSeenUserEvent = userEvent
    return shouldReact
  }

  private fun shouldReact(userEvent: UserEvent): Boolean {
    val otherUserEvent = lastSeenUserEvent
    return if (config.minimalMode.not() || otherUserEvent == null) {
      true
    } else {
      isDifferent(otherUserEvent, userEvent)
    }
  }

  private fun isDifferent(
    aUserEvent: UserEvent,
    anotherUserEvent: UserEvent
  ): Boolean = aUserEvent.type != anotherUserEvent.type ||
    aUserEvent.category != anotherUserEvent.category

  fun updateConfig(config: Config): ModalityProcessor =
    ModalityProcessor(config).let {
      it.lastSeenUserEvent = this.lastSeenUserEvent
      it
    }
}
