package io.unthrottled.amii

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import io.unthrottled.amii.assets.AnimeContentManager
import io.unthrottled.amii.assets.AudibleContentManager
import io.unthrottled.amii.assets.CacheWarmingService
import io.unthrottled.amii.assets.CharacterContentManager
import io.unthrottled.amii.assets.LocalVisualContentManager
import io.unthrottled.amii.assets.RemoteVisualContentManager
import io.unthrottled.amii.assets.Status
import io.unthrottled.amii.listeners.IdleEventListener
import io.unthrottled.amii.listeners.SilenceListener
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.onboarding.UserOnBoarding
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.services.WelcomeService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.stream.Stream

class PluginMaster : Disposable, Logging {

  companion object {
    val instance: PluginMaster
      get() = ApplicationManager.getApplication().getService(PluginMaster::class.java)
  }

  private val projectListeners: ConcurrentMap<String, ProjectListeners> = ConcurrentHashMap()
  private val projectRegistry = ProjectLifecycleRegistry()

  init {
    CacheWarmingService.instance.init()
    LocalVisualContentManager.init()
  }

  fun projectOpened(project: Project) {
    registerListenersForProject(project)
  }

  @Synchronized
  private fun registerListenersForProject(project: Project) {
    if (project.isDisposed) return

    val projectId = project.locationHash
    if (projectRegistry.markProjectOpened(projectId, project.isDisposed).not()) return

    projectListeners[projectId] = ProjectListeners(project)
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    WelcomeService.greetUser(project)
    checkIfInGoodState(project)
  }

  private fun checkIfInGoodState(project: Project) {
    val isInGoodState = Stream.of(
      AudibleContentManager,
      RemoteVisualContentManager,
      AnimeContentManager,
      CharacterContentManager
    ).map { it.status }
      .allMatch { it == Status.OK }
    if (!isInGoodState) {
      UpdateNotification.sendMessage(
        PluginMessageBundle.message("notifications.bad.state.title"),
        PluginMessageBundle.message("notifications.bad.state.body"),
        project
      )
    }
  }

  fun projectClosed(project: Project) {
    projectRegistry.markProjectClosed(project.locationHash)
    projectListeners.remove(project.locationHash)?.dispose()
  }

  override fun dispose() {
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
    LifeCycleManager.dispose()
  }

  fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .filter { it.isDisposed.not() }
      .forEach { registerListenersForProject(it) }
  }
}

internal class ProjectLifecycleRegistry {
  private val openProjects = ConcurrentHashMap.newKeySet<String>()

  @Synchronized
  fun markProjectOpened(projectId: String, isDisposed: Boolean): Boolean {
    if (isDisposed) return false

    return openProjects.add(projectId)
  }

  fun markProjectClosed(projectId: String) {
    openProjects.remove(projectId)
  }
}

internal data class ProjectListeners(
  private val project: Project
) : Disposable {

  private val idleEventListener = IdleEventListener(project)
  private val silenceListener = SilenceListener(project)

  override fun dispose() {
    idleEventListener.dispose()
    silenceListener.dispose()
  }
}
