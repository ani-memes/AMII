package io.unthrottled.amii

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Disposer
import io.unthrottled.amii.listeners.IdleEventListener
import io.unthrottled.amii.listeners.PLUGIN_UPDATE_TOPIC
import io.unthrottled.amii.listeners.PluginUpdateListener
import io.unthrottled.amii.onboarding.UserOnBoarding
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class PluginMaster :
  ProjectManagerListener, PluginUpdateListener, Disposable {

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()

  init {
    ApplicationManager.getApplication()
      .invokeLater {
        ApplicationManager.getApplication()
          .messageBus
          .connect(this)
          .subscribe(PLUGIN_UPDATE_TOPIC, this)
      }
  }

  override fun projectOpened(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    val projectId = project.locationHash
    if (projectListeners.containsKey(projectId).not()) {
      projectListeners[projectId] =
        ProjectListeners(project)
    }
  }

  override fun projectClosed(project: Project) {
    projectListeners[project.locationHash]?.dispose()
  }

  override fun dispose() {
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
  }

  override fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}

internal data class ProjectListeners(
  private val project: Project,
) : Disposable {

  private val idleEventListener = IdleEventListener(project)

  init {
    Disposer.register(this, idleEventListener)
  }

  override fun dispose() {
  }
}
