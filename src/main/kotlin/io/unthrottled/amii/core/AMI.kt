package io.unthrottled.amii.core

import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.memes.MemeFactory
import java.util.Optional

class AMI : UserEventListener {

  private val log = Logger.getInstance(this::class.java)

  override fun onDispatch(userEvent: UserEvent) {
    log.warn("Seen user event $userEvent")
    when (userEvent.eventName) {
      "Show Random" -> MemeFactory.createMemeDisplay(userEvent.project)
      else -> Optional.empty()
    }.ifPresent {
      it.display()
    }
  }
}
