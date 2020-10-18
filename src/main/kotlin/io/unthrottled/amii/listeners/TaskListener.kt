package io.unthrottled.amii.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class TaskListener(private val project: Project) : ProjectTaskListener {

  private val log = Logger.getInstance(this::class.java)

  private var previousTaskStatus = TaskStatus.UNKNOWN

  override fun finished(result: ProjectTaskManager.Result) {
    when {
      result.hasErrors() -> {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent("Task Error", project),
          )
        log.warn("Observed task error")
      }
      previousTaskStatus == TaskStatus.FAIL -> {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EVENT_TOPIC)
          .onDispatch(
            UserEvent("Task Success after Error", project),
          )
        log.info("Observed task success after failure")
      }
      else -> {
        previousTaskStatus = TaskStatus.PASS
      }
    }
  }
}
