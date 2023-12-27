package io.unthrottled.amii.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.InputRedirectAware
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

fun ExecutionEnvironment.isJUnit(): Boolean {
  val runProfile = this.runProfile
  return runProfile is InputRedirectAware &&
    runProfile.type.displayName.equals("JUnit", ignoreCase = true)
}

data class NamedProcess(
  val type: NamedProcesses,
  val id: Long
)

enum class NamedProcesses {
  JUNIT, OTHER,
}

interface ProcessLifecycleListener {

  fun onProcessStarted(startedProcess: NamedProcess) {}

  fun onProcessCompleted(completedProcess: NamedProcess) {}
}

val PROCESS_LIFECYCLE_TOPIC = Topic.create(
  "Process Lifecycle",
  ProcessLifecycleListener::class.java
)

class ProcessExecutionListener(private val project: Project) : ExecutionListener {

  override fun processStarting(executorId: String, env: ExecutionEnvironment) {
    project.messageBus
      .syncPublisher(PROCESS_LIFECYCLE_TOPIC)
      .onProcessStarted(
        NamedProcess(
          if (env.isJUnit()) {
            NamedProcesses.JUNIT
          } else {
            NamedProcesses.OTHER
          },
          env.executionId
        )
      )
  }

  override fun processTerminated(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ) {
    project.messageBus
      .syncPublisher(PROCESS_LIFECYCLE_TOPIC)
      .onProcessCompleted(
        NamedProcess(
          if (env.isJUnit()) {
            NamedProcesses.JUNIT
          } else {
            NamedProcesses.OTHER
          },
          env.executionId
        )
      )
  }
}
