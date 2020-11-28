package io.unthrottled.amii.core

import com.intellij.openapi.Disposable
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.memes.MemeFactory
import io.unthrottled.amii.tools.AlarmDebouncer
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import java.util.Optional

class AMI : UserEventListener, Disposable, Logging {

  companion object {
    private const val DEBOUNCE_INTERVAL = 80
  }

  private val singleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL, this)
  private val idleEventDebouncer = AlarmDebouncer<UserEvent>(DEBOUNCE_INTERVAL, this)

  override fun onDispatch(userEvent: UserEvent) {
    logger().warn("Seen user event $userEvent")
    when (userEvent.eventName) {
      "Show Random" -> MemeFactory.createMemeDisplay(userEvent.project)
      else -> Optional.empty()
    }.ifPresent {
      it.display()
    }
  }

  override fun dispose() {
  }
}
