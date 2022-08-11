package io.unthrottled.amii.listeners

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo
import com.intellij.openapi.project.Project
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.services.ProcessHandlerService.wasCanceled
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.logger

class TestEventListener(private val project: Project) : SMTRunnerEventsAdapter(), Logging {

  override fun onTestingFinished(testsRoot: SMRootTestProxy) {
    logger().debug("Test finished {}".format(testsRoot))
    if (shouldEmitEvent(testsRoot)) {
      emitTestEvent(testsRoot)
    }
  }

  private fun emitTestEvent(testsRoot: SMRootTestProxy) {
    val (type, category) = if (isSuccess(testsRoot)) {
      PluginMessageBundle.message("user.event.test.pass.name") to UserEventCategory.POSITIVE
    } else {
      PluginMessageBundle.message("user.event.test.fail.name") to UserEventCategory.NEGATIVE
    }

    project.messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(
          UserEvents.TEST,
          category,
          type,
          project
        )
      )
  }

  private fun isSuccess(testsRoot: SMRootTestProxy): Boolean =
    (testsRoot.isPassed || isSuccessWithIgnoredTests(testsRoot)) &&
      (testsRoot.handler?.exitCode ?: 0) == 0

  private fun isSuccessWithIgnoredTests(testsRoot: SMRootTestProxy): Boolean =
    TestStateInfo.Magnitude.IGNORED_INDEX == testsRoot.magnitudeInfo

  private fun shouldEmitEvent(testsRoot: SMRootTestProxy): Boolean =
    !(
      testsRoot.wasTerminated() ||
        testsRoot.isInterrupted ||
        wasCanceled(testsRoot.handler)
      )
}
