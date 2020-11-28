package io.unthrottled.amii.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class TaskListener(private val project: Project) : ProjectTaskListener, Logging {

  private var previousTaskStatus = TaskStatus.UNKNOWN

  override fun finished(result: ProjectTaskManager.Result) {
    when {
      result.hasErrors() -> {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.NEGATIVE,
              "Task Error",
              project
            ),
          )
        logger().warn("Observed task error")
      }
      previousTaskStatus == TaskStatus.FAIL -> {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent(
              UserEvents.TASK,
              UserEventCategory.POSITIVE,
              "Task Success after Error",
              project
            ),
          )
        logger().info("Observed task success after failure")
      }
      else -> {
        previousTaskStatus = TaskStatus.PASS
      }
    }
  }
}
