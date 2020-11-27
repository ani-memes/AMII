package io.unthrottled.amii.listeners

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.services.ProcessHandlerService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger

class TestEventListener(private val project: Project) : SMTRunnerEventsAdapter(), Logging {

  override fun onTestingFinished(testsRoot: SMRootTestProxy) {
    logger().info("Test finished {}".format(testsRoot))
    if (shouldEmitEvent(testsRoot)) {
      emitTestEvent(testsRoot)
    }
  }

  private fun emitTestEvent(testsRoot: SMRootTestProxy) {
    val type = if (isSuccess(testsRoot)) {
      "Test Success"
    } else {
      "Test Failure"
    }

    ApplicationManager.getApplication().messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(type, project)
      )
  }

  private fun isSuccess(testsRoot: SMRootTestProxy): Boolean =
    testsRoot.isPassed || isSuccessWithIgnoredTests(testsRoot)

  private fun isSuccessWithIgnoredTests(testsRoot: SMRootTestProxy): Boolean =
    TestStateInfo.Magnitude.IGNORED_INDEX == testsRoot.magnitudeInfo

  private fun shouldEmitEvent(testsRoot: SMRootTestProxy): Boolean =
    !(
      testsRoot.wasTerminated() ||
        testsRoot.isInterrupted ||
        ProcessHandlerService.wasCanceled(testsRoot.handler)
      )
}
