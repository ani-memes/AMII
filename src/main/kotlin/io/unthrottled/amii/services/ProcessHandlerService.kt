package io.unthrottled.amii.services

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskState
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler

object ProcessHandlerService {

  private val ignoredStates = setOf(
    ExternalSystemTaskState.CANCELED,
    ExternalSystemTaskState.CANCELING
  )

  fun wasCanceled(handler: ProcessHandler): Boolean =
    handler is ExternalSystemProcessHandler &&
      ignoredStates.contains(handler.task?.state)
}
