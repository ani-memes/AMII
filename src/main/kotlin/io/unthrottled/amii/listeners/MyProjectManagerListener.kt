package io.unthrottled.amii.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

  override fun projectOpened(project: Project) {
    project.getService(MyProjectService::class.java)
  }
}
