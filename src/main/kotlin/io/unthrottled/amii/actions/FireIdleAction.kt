package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle

class FireIdleAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    project?.messageBus
      ?.syncPublisher(EVENT_TOPIC)
      ?.onDispatch(
        UserEvent(
          UserEvents.IDLE,
          UserEventCategory.NEUTRAL,
          PluginMessageBundle.message("user.event.idle.name"),
          project
        )
      )
  }
}
