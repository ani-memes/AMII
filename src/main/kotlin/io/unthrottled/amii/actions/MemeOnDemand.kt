package io.unthrottled.amii.actions

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.AlarmDebouncer
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle

class MemeOnDemand : AnAction(), DumbAware, Logging, Disposable {

  companion object {
    private const val DEMAND_DELAY = 250
  }

  private val debouncer = AlarmDebouncer<AnActionEvent>(DEMAND_DELAY)

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project!!
    debouncer.debounce {
      ApplicationManager.getApplication().messageBus
        .syncPublisher(EVENT_TOPIC)
        .onDispatch(
          UserEvent(
            UserEvents.ON_DEMAND,
            UserEventCategory.NEUTRAL,
            PluginMessageBundle.message("user.event.on-demand.name"),
            project
          )
        )
    }
  }

  override fun dispose() {
  }
}
