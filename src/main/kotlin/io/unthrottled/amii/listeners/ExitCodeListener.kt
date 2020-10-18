package io.unthrottled.amii.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.CONFIG_TOPIC
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.Config.Companion.DEFAULT_DELIMITER
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent

const val OK_EXIT_CODE = 0
const val FORCE_KILLED_EXIT_CODE = 130

fun String.toExitCodes(): Set<Int> = this.split(DEFAULT_DELIMITER)
  .filter { it.isNotEmpty() }
  .map { it.trim().toInt() }.toSet()

class ExitCodeListener(private val project: Project) : ExecutionListener, Disposable {
  private val log = Logger.getInstance(javaClass)
  private val messageBus = ApplicationManager.getApplication().messageBus.connect()

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
    messageBus.dispose()
  }

  override fun processTerminated(
    executorId: String,
    env: ExecutionEnvironment,
    handler: ProcessHandler,
    exitCode: Int
  ) {
    log.debug("Observed exit code of $exitCode")
    if (allowedExitCodes.contains(exitCode).not() && env.project == project) {
      log.info("Should do something with exit code: $exitCode")
      ApplicationManager.getApplication().messageBus
        .syncPublisher(EVENT_TOPIC)
        .onDispatch(
          UserEvent("Exit code", project)
        )
    }
  }
}
