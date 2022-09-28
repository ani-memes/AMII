package io.unthrottled.amii.services

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import io.unthrottled.amii.extensions.ExitCodeFilter

class ExitCodeFilterService {
  fun shouldProcess(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int): Boolean =
    EP_NAME.extensionList
      .all {
        exitCodeFilter ->
        exitCodeFilter.shouldProcess(
          executorId, env, handler, exitCode
        )
      }

  fun shouldProcess(testProxy: SMTestProxy.SMRootTestProxy): Boolean {
    return EP_NAME.extensionList
      .all {
        exitCodeFilter ->
        exitCodeFilter.shouldProcess(testProxy)
      }
  }

  companion object {
    private val EP_NAME =
      ExtensionPointName.create<ExitCodeFilter>("io.unthrottled.amii.exitCodeFilter")

    val instance: ExitCodeFilterService
      get() = ApplicationManager.getApplication().getService(ExitCodeFilterService::class.java)
  }
}
