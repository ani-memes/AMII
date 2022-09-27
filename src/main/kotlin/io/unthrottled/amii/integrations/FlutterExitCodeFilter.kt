package io.unthrottled.amii.integrations

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import io.unthrottled.amii.extensions.ExitCodeFilter

class FlutterExitCodeFilter: ExitCodeFilter {
  override fun shouldProcess(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ): Boolean {
    return true
  }
}
