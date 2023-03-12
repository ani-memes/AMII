package io.unthrottled.amii

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import io.unthrottled.amii.assets.*
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

  init {
    CacheWarmingService.instance.init()
    LocalVisualContentManager.init()
  }

  fun projectOpened(project: Project) {
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    val projectId = project.locationHash
    if (projectListeners.containsKey(projectId).not()) {
      WelcomeService.greetUser(project)
      projectListeners[projectId] =
        ProjectListeners(project)
      checkIfInGoodState(project)
    }
  }

  private fun checkIfInGoodState(project: Project) {
    val isInGoodState = Stream.of(
      AudibleContentManager,
      RemoteVisualContentManager,
      AnimeContentManager,
      CharacterContentManager,
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
    projectListeners[project.locationHash]?.dispose()
    projectListeners.remove(project.locationHash)
  }

  override fun dispose() {
    projectListeners.forEach { (_, listeners) -> listeners.dispose() }
    LifeCycleManager.dispose()
  }

  fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}

internal data class ProjectListeners(
  private val project: Project,
) : Disposable {

  private val idleEventListener = IdleEventListener(project)
  private val silenceListener = SilenceListener(project)

  override fun dispose() {
    idleEventListener.dispose()
    silenceListener.dispose()
  }
}
