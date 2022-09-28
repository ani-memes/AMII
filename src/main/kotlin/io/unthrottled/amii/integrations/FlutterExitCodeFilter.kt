package io.unthrottled.amii.integrations

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import io.flutter.utils.MostlySilentColoredProcessHandler
import io.unthrottled.amii.extensions.ExitCodeFilter

class FlutterExitCodeFilter : ExitCodeFilter {
  override fun shouldProcess(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ): Boolean {
    if (handler !is MostlySilentColoredProcessHandler) {
      return true
    }

    val isTerminatedDebugProcess =
      executorId == "Debug" &&
        exitCode == -1 &&
        handler.commandLine.contains("flutter.bat")
    return !isTerminatedDebugProcess
  }

  override fun shouldProcess(testProxy: SMTestProxy.SMRootTestProxy): Boolean {
    val handler = testProxy.handler
    if (handler !is MostlySilentColoredProcessHandler) {
      return true
    }

    return false
  }
}
