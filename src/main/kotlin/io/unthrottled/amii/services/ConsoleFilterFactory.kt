package io.unthrottled.amii.services

import com.intellij.execution.filters.Filter
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.listeners.NamedProcess
import io.unthrottled.amii.listeners.NamedProcesses
import io.unthrottled.amii.listeners.PROCESS_LIFECYCLE_TOPIC
import io.unthrottled.amii.listeners.ProcessLifecycleListener
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

class ConsoleFilterFactory(
  private val project: Project
) : Logging, Disposable {

  private var shouldEmit = true
  private val runningProcesses = mutableSetOf<NamedProcess>()

  init {
    project.messageBus.connect(this)
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

  fun getFilter(): Optional<Filter> {
    return if (shouldEmit && runningProcesses.isNotEmpty()) {
      Filter { line, _ ->
        if (
          shouldEmit &&
          runningProcesses.isNotEmpty() &&
          line.contains("started on port")
        ) {
          logger().warn("I see the thing you where looking for in logs!")
          ApplicationManager.getApplication().messageBus
            .syncPublisher(EVENT_TOPIC)
            .onDispatch(
              UserEvent(
                UserEvents.LOGS,
                UserEventCategory.NEUTRAL,
                "Log Event",
                project
              )
            )
        }
        null
      }
    } else {
      null
    }.toOptional()
  }

  override fun dispose() {
  }
}
