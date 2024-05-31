package io.unthrottled.amii.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupActivity
import io.unthrottled.amii.PluginMaster
import io.unthrottled.amii.tools.Logging

internal class PluginPostStartUpActivity : StartupActivity, ProjectActivity {
  override fun runActivity(project: Project) {
    PluginMaster.instance.projectOpened(project)
  }

  override suspend fun execute(project: Project) {
    runActivity(project)
  }
}

internal class ProjectListener :
  ProjectManagerListener, Logging {

  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}
