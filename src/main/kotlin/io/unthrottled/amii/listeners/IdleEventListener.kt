package io.unthrottled.amii.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.BalloonTools
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.runSafely
import java.awt.AWTEvent
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.util.concurrent.TimeUnit

class IdleEventListener(private val project: Project) : Runnable, Disposable, AWTEventListener {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val log = Logger.getInstance(this::class.java)
  private lateinit var _rootPane: Component

  private fun getRootPane(): Component? {
    return if (this::_rootPane.isInitialized) {
      _rootPane
    } else {
      val frame = BalloonTools.getIDEFrame(project)
        .orElse(null)
      if (frame !== null) {
        _rootPane = frame.component
      }
      frame?.component
    }
  }

  private var idleTimeout =
    TimeUnit.MILLISECONDS.convert(
      getCurrentTimoutInMinutes(),
      TimeUnit.MINUTES
    ).toInt()
  private val idleAlarm = Alarm()

  init {
    val self = this
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        idleAlarm.cancelAllRequests()
        self.idleTimeout = TimeUnit.MILLISECONDS.convert(
          newPluginState.idleTimeoutInMinutes,
          TimeUnit.MINUTES
        ).toInt()
        idleAlarm.addRequest(self, self.idleTimeout)
      }
    )
    Toolkit.getDefaultToolkit().addAWTEventListener(
      this,
      AWTEvent.MOUSE_EVENT_MASK or
        AWTEvent.KEY_EVENT_MASK
    )
    idleAlarm.addRequest(this, idleTimeout)
  }

  private fun getCurrentTimoutInMinutes(): Long =
    Config.instance.idleTimeoutInMinutes

  override fun dispose() {
    messageBus.dispose()
    idleAlarm.dispose()
    runSafely({
      Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }) {}
  }

  override fun run() {
    log.debug("Observed idled timeout")
    project.messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(
          UserEvents.IDLE,
          UserEventCategory.NEUTRAL,
          PluginMessageBundle.message("user.event.idle.name"),
          project
        )
      )
    idleAlarm.addRequest(this, idleTimeout)
  }

  private val allowedMouseEvents =
    MouseEvent.MOUSE_PRESSED or
      MouseEvent.MOUSE_CLICKED

  override fun eventDispatched(e: AWTEvent) {
    if (e !is InputEvent || isInWindow(e)) return

    if (e is MouseEvent && (allowedMouseEvents and e.id) != e.id) return

    idleAlarm.cancelAllRequests()
    idleAlarm.addRequest(this, idleTimeout)
  }

  private fun isInWindow(e: InputEvent): Boolean {
    val rootPane = getRootPane()
    return if (rootPane == null) {
      false
    } else {
      !UIUtil.isDescendingFrom(e.component, rootPane)
    }
  }
}
