package io.unthrottled.amii

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.amii.listeners.IdleEventListener
import io.unthrottled.amii.onboarding.UserOnBoarding
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class PluginMaster : ProjectManagerListener, Disposable {

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()

  override fun projectOpened(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    projectListeners[project.locationHash] =
      ProjectListeners(
        IdleEventListener(project)
      )
  }

  override fun projectClosed(project: Project) {
    projectListeners[project.locationHash]?.dispose()
  }

  override fun dispose() {
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
  }
}

internal data class ProjectListeners(
  val idleEventListener: IdleEventListener
) : Disposable {
  override fun dispose() {
    idleEventListener.dispose()
  }
}
