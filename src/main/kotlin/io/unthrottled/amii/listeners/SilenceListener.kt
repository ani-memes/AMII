package io.unthrottled.amii.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle
import java.util.concurrent.TimeUnit

class SilenceListener(private val project: Project) : Runnable, UserEventListener, Disposable {
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()
  private val log = Logger.getInstance(this::class.java)
  private val silenceAlarm = Alarm()

  init {
    val self = this
    messageBus.subscribe(EVENT_TOPIC, this)
    messageBus.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        silenceAlarm.cancelAllRequests()
        silenceAlarm.addRequest(
          self,
          TimeUnit.MILLISECONDS.convert(
            newPluginState.silenceTimeoutInMinutes,
            TimeUnit.MINUTES
          ).toInt()
        )
      }
    )
    scheduleSilenceAlert()
  }

  private fun scheduleSilenceAlert() {
    silenceAlarm.addRequest(
      this,
      TimeUnit.MILLISECONDS.convert(
        getCurrentTimoutInMinutes(),
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private fun getCurrentTimoutInMinutes(): Long =
    Config.instance.silenceTimeoutInMinutes

  override fun dispose() {
    messageBus.dispose()
    silenceAlarm.dispose()
  }

  override fun run() {
    log.debug("Observed silence timeout")
    ApplicationManager.getApplication().messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(
          UserEvents.SILENCE,
          UserEventCategory.NEUTRAL,
          PluginMessageBundle.message("user.event.silence.name"),
          project
        )
      )
  }

  override fun onDispatch(userEvent: UserEvent) {
    when (userEvent.type) {
      UserEvents.IDLE -> silenceAlarm.cancelAllRequests()
      else -> {
        silenceAlarm.cancelAllRequests()
        scheduleSilenceAlert()
      }
    }
  }
}
