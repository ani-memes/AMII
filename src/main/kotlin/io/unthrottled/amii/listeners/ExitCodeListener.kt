package io.unthrottled.amii.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.InputRedirectAware
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.Config.Companion.DEFAULT_DELIMITER
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.listeners.NamedProcesses.JUNIT
import io.unthrottled.amii.services.ProcessHandlerService.wasCanceled

const val OK_EXIT_CODE = 0
const val FORCE_KILLED_EXIT_CODE = 130

fun String.toExitCodes(): Set<Int> = this.split(DEFAULT_DELIMITER)
  .filter { it.isNotEmpty() }
  .map { it.trim().toInt() }.toSet()

fun ExecutionEnvironment.isJUnit(): Boolean {
  val runProfile = this.runProfile
  return runProfile is InputRedirectAware &&
    runProfile.type.displayName.equals("JUnit", ignoreCase = true)
}

enum class NamedProcesses {
  JUNIT, OTHER,
}

interface ProcessLifecycleListener {

  fun onProcessStarted(startedProcess: NamedProcesses) {}

  fun onProcessCompleted(completedProcess: NamedProcesses) {}
}

val PROCESS_LIFECYCLE_TOPIC = Topic.create(
  "Process Lifecycle",
  ProcessLifecycleListener::class.java
)

class ExitCodeListener(private val project: Project) : ExecutionListener, Disposable {
  private val log = Logger.getInstance(javaClass)
  private val messageBus = ApplicationManager.getApplication().messageBus.connect(this)

  private var allowedExitCodes = Config.instance
    .allowedExitCodes.toExitCodes()

  init {
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        allowedExitCodes = newPluginState
          .allowedExitCodes.toExitCodes()
      }
    )
  }

  override fun dispose() {
  }

  override fun processStarting(executorId: String, env: ExecutionEnvironment) {
    if (env.isJUnit()) {
      project.messageBus
        .syncPublisher(PROCESS_LIFECYCLE_TOPIC)
        .onProcessStarted(JUNIT)
    }
  }

  override fun processTerminated(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ) {
    if (env.isJUnit()) {
      project.messageBus
        .syncPublisher(
          PROCESS_LIFECYCLE_TOPIC
        ).onProcessCompleted(JUNIT)
    }

    log.debug("Observed exit code of $exitCode")
    if (wasCanceled(handler).not() &&
      allowedExitCodes.contains(exitCode).not() &&
      env.project == project
    ) {
      log.info("Should do something with exit code: $exitCode")
      ApplicationManager.getApplication().messageBus
        .syncPublisher(EVENT_TOPIC)
        .onDispatch(
          UserEvent("Exit code", project)
        )
    }
  }
}
