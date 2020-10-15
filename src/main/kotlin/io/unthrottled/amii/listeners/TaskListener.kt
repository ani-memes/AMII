package io.unthrottled.amii.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager

internal enum class TaskStatus {
  PASS, FAIL, UNKNOWN
}

class TaskListener(private val project: Project) : ProjectTaskListener {

  private val log = Logger.getInstance(this::class.java)

  private var previousTaskStatus = TaskStatus.UNKNOWN

  override fun finished(result: ProjectTaskManager.Result) {
    when {
      result.hasErrors() -> {
        log.warn("Observed task error")
      }
      previousTaskStatus == TaskStatus.FAIL -> {
        log.info("Observed task success after failure")
      }
      else -> {
        previousTaskStatus = TaskStatus.PASS
      }
    }
  }
}
