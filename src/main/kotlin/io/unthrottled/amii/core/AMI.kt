package io.unthrottled.amii.core

import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventListener

class AMI : UserEventListener {

  private val log = Logger.getInstance(this::class.java)

  override fun onDispatch(userEvent: UserEvent) {
    log.warn("Seen user event $userEvent")
  }
}
