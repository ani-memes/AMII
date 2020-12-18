package io.unthrottled.amii.listeners

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle
import java.util.concurrent.TimeUnit

class IdleEventListener(private val project: Project) : Runnable, Disposable {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val log = Logger.getInstance(this::class.java)

  init {
    val self = this
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        IdeEventQueue.getInstance().removeIdleListener(self)
        IdeEventQueue.getInstance().addIdleListener(
          self,
          TimeUnit.MILLISECONDS.convert(
            newPluginState.idleTimeoutInMinutes,
            TimeUnit.MINUTES
          ).toInt()
        )
      }
    )
    IdeEventQueue.getInstance().addIdleListener(
      this,
      TimeUnit.MILLISECONDS.convert(
        getCurrentTimoutInMinutes(),
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private fun getCurrentTimoutInMinutes(): Long =
    Config.instance.idleTimeoutInMinutes

  override fun dispose() {
    messageBus.dispose()
    IdeEventQueue.getInstance().removeIdleListener(this)
  }

  override fun run() {
    log.info("Observed idled timeout")
    ApplicationManager.getApplication().messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(
          UserEvents.IDLE,
          UserEventCategory.NEUTRAL,
          PluginMessageBundle.message("user.event.idle.name"),
          project
        )
      )
  }
}
