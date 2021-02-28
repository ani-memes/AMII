package io.unthrottled.amii.core

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEvents

class ModalityProcessor(private val config: Config) {

  private var lastSeenUserEvent: UserEvent? = null

  fun shouldProcess(userEvent: UserEvent): Boolean {
    if(userEvent.project.isDisposed) return false

    val shouldReact = shouldReact(userEvent)
    lastSeenUserEvent = userEvent
    return shouldReact
  }

  private fun shouldReact(userEvent: UserEvent): Boolean {
    val otherUserEvent = lastSeenUserEvent
    return if (
      config.minimalMode.not() ||
      isWhitelisted(userEvent) ||
      otherUserEvent == null
    ) {
      true
    } else {
      isDifferent(otherUserEvent, userEvent)
    }
  }

  private val whiteListedEvents = setOf(
    UserEvents.IDLE,
    UserEvents.RETURN,
  )
  private fun isWhitelisted(userEvent: UserEvent) =
    whiteListedEvents.contains(userEvent.type)

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
