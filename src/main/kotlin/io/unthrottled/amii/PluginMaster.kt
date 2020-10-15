package io.unthrottled.amii

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.onboarding.UserOnBoarding
import io.unthrottled.amii.services.MyProjectService

internal class PluginMaster : ProjectManagerListener {

  override fun projectOpened(project: Project) {
    project.getService(MyProjectService::class.java)
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
  }
}
