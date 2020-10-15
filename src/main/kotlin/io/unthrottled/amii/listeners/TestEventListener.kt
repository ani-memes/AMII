package io.unthrottled.amii.listeners

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class TestEventListener(private val project: Project) : SMTRunnerEventsAdapter() {

  private val log = Logger.getInstance(this::class.java)

  override fun onTestingFinished(testsRoot: SMTestProxy.SMRootTestProxy) {
    log.info("Test finished {}".format(testsRoot))
  }
}
