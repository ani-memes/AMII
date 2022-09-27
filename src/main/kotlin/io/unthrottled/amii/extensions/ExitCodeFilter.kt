package io.unthrottled.amii.extensions

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

interface ExitCodeFilter {
  fun shouldProcess(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ): Boolean

}
