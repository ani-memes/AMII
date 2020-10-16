package io.unthrottled.amii.listeners

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent

class TestEventListener(private val project: Project) : SMTRunnerEventsAdapter() {

  private val log = Logger.getInstance(this::class.java)

  override fun onTestingFinished(testsRoot: SMTestProxy.SMRootTestProxy) {
    log.info("Test finished {}".format(testsRoot))
    if (shouldEmitEvent(testsRoot)) {
      emitTestEvent(testsRoot)
    }
  }

  private fun emitTestEvent(testsRoot: SMTestProxy.SMRootTestProxy) {
    val type = if (isSuccess(testsRoot)) {
      "Test Success"
    } else {
      "Test Failure"
    }

    ApplicationManager.getApplication().messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent(type)
      )
  }

  private fun isSuccess(testsRoot: SMTestProxy.SMRootTestProxy): Boolean =
    testsRoot.isPassed || isSuccessWithIgnoredTests(testsRoot)

  private fun isSuccessWithIgnoredTests(testsRoot: SMRootTestProxy): Boolean =
    TestStateInfo.Magnitude.IGNORED_INDEX == testsRoot.magnitudeInfo

  // todo: re-visit this
  private fun shouldEmitEvent(testsRoot: SMTestProxy.SMRootTestProxy): Boolean =
    !(testsRoot.wasTerminated() || testsRoot.isInterrupted)
}
