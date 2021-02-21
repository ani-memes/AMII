package io.unthrottled.amii.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEventListener
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class TaskListener(private val project: Project) :
  ProjectTaskListener,
  UserEventListener,
  Disposable,
  Logging {

  private var previousTaskStatus = TaskStatus.UNKNOWN

  private val messageBusConnection = project.messageBus.connect()

  init {
    ApplicationManager.getApplication().invokeLater {
      messageBusConnection.subscribe(EVENT_TOPIC, this)
    }
  }

  override fun finished(result: ProjectTaskManager.Result) {
    when {
      result.hasErrors() -> {
        project.messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.NEGATIVE,
              PluginMessageBundle.message("user.event.task.failure.name"),
              project
            ),
          )
      }
      previousTaskStatus == TaskStatus.FAIL -> {
        project.messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.POSITIVE,
              PluginMessageBundle.message("user.event.task.success.name"),
              project
            ),
          )
      }
    }
  }

  override fun onDispatch(userEvent: UserEvent) {
    previousTaskStatus = when (userEvent.type) {
      UserEvents.TASK -> {
        when (userEvent.category) {
          UserEventCategory.NEGATIVE -> TaskStatus.FAIL
          UserEventCategory.POSITIVE -> TaskStatus.PASS
          else -> TaskStatus.UNKNOWN
        }
      }
      else -> TaskStatus.UNKNOWN
    }
  }

  override fun dispose() {
    messageBusConnection.dispose()
  }
}
