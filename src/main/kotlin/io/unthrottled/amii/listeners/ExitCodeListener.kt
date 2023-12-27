package io.unthrottled.amii.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.Config.Companion.DEFAULT_DELIMITER
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.services.ExitCodeFilterService
import io.unthrottled.amii.services.ProcessHandlerService.wasCanceled
import io.unthrottled.amii.tools.PluginMessageBundle

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

  private var positiveExitCodes = Config.instance
    .positiveExitCodes.toExitCodes()

  init {
    messageBus.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        allowedExitCodes = newPluginState
          .allowedExitCodes.toExitCodes()
        positiveExitCodes = newPluginState
          .positiveExitCodes.toExitCodes()
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
    if (wasCanceled(handler) || env.project != project ||
      ExitCodeFilterService.instance.shouldProcess(
          executorId,
          env,
          handler,
          exitCode
        ).not()
    ) {
      return
    }

    if (positiveExitCodes.contains(exitCode)) {
      log.debug("Should react positively to exit code: $exitCode")
      dispatchExitCodeEvent(UserEventCategory.POSITIVE)
    } else if (allowedExitCodes.contains(exitCode).not()) {
      log.debug("Should react negatively to exit code: $exitCode")
      dispatchExitCodeEvent(UserEventCategory.NEGATIVE)
    }
  }

  private fun dispatchExitCodeEvent(category: UserEventCategory) {
    project.messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(
          UserEvents.PROCESS,
          category,
          PluginMessageBundle.message("user.event.exit-code.name"),
          project
        )
      )
  }
}
