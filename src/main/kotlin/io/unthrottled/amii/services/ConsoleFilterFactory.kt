package io.unthrottled.amii.services

import com.intellij.execution.filters.Filter
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.listeners.NamedProcess
import io.unthrottled.amii.listeners.NamedProcesses
import io.unthrottled.amii.listeners.PROCESS_LIFECYCLE_TOPIC
import io.unthrottled.amii.listeners.ProcessLifecycleListener
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

class ConsoleFilterFactory(
  private val project: Project
) : Logging, Disposable {

  private var shouldEmit = true
  private val runningProcesses = mutableSetOf<NamedProcess>()
  private var keyword = Config.instance.logSearchTerms
  private var ignoreCase = Config.instance.logSearchIgnoreCase

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBusConnection.subscribe(
      CONFIG_TOPIC,
      ConfigListener {
        keyword = it.logSearchTerms
        ignoreCase = it.logSearchIgnoreCase
      }
    )
    messageBusConnection
      .subscribe(
        PROCESS_LIFECYCLE_TOPIC,
        object : ProcessLifecycleListener {
          override fun onProcessStarted(startedProcess: NamedProcess) {
            runningProcesses.add(startedProcess)
            if (startedProcess.type != NamedProcesses.JUNIT) return
            shouldEmit = false
          }

          override fun onProcessCompleted(completedProcess: NamedProcess) {
            runningProcesses.remove(completedProcess)
            if (completedProcess.type != NamedProcesses.JUNIT) return
            shouldEmit = true
          }
        }
      )
  }

  fun getFilter(): Optional<Filter> =
    if (shouldEmit &&
      runningProcesses.isNotEmpty() &&
      keyword.isNotBlank()
    ) {
      Filter { line, _ ->
        if (
          shouldEmit &&
          runningProcesses.isNotEmpty() &&
          line.contains(keyword, ignoreCase = ignoreCase)
        ) {
          project.messageBus
            .syncPublisher(EVENT_TOPIC)
            .onDispatch(
              UserEvent(
                UserEvents.LOGS,
                UserEventCategory.NEUTRAL,
                PluginMessageBundle.message("user.event.log-watch.name"),
                project
              )
            )
        }
        null
      }
    } else {
      null
    }.toOptional()

  override fun dispose() {
    messageBusConnection.dispose()
  }
}
