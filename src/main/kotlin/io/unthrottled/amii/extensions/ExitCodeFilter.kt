package io.unthrottled.amii.extensions

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTestProxy

interface ExitCodeFilter {
  fun shouldProcess(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ): Boolean

  fun shouldProcess(testProxy: SMTestProxy.SMRootTestProxy): Boolean
}
