package io.unthrottled.amii.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.util.concurrency.AppExecutorUtil
import io.unthrottled.amii.assets.AnimeContentManager
import io.unthrottled.amii.assets.AudibleContentManager
import io.unthrottled.amii.assets.CharacterContentManager
import io.unthrottled.amii.assets.RemoteVisualContentManager
import io.unthrottled.amii.assets.Status
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.tools.PluginMessageBundle
import java.util.concurrent.TimeUnit

object WelcomeService {
  private const val MAX_READINESS_ATTEMPTS = 5
  private const val READINESS_RETRY_DELAY_MILLIS = 500L

  fun greetUser(project: Project) {
    StartupManager.getInstance(project)
      .runWhenProjectIsInitialized {
        dispatchGreetingWhenReady(project, 0)
      }
  }

  private fun dispatchGreetingWhenReady(project: Project, attempt: Int) {
    if (project.isDisposed) return

    if (assetMetadataResolved() || attempt >= MAX_READINESS_ATTEMPTS) {
      dispatchGreeting(project)
      return
    }

    AppExecutorUtil.getAppScheduledExecutorService().schedule(
      {
        dispatchGreetingWhenReady(project, attempt + 1)
      },
      READINESS_RETRY_DELAY_MILLIS,
      TimeUnit.MILLISECONDS
    )
  }

  private fun assetMetadataResolved(): Boolean =
    listOf(
      AudibleContentManager.status,
      RemoteVisualContentManager.status,
      AnimeContentManager.status,
      CharacterContentManager.status
    ).none { it == Status.UNKNOWN }

  private fun dispatchGreeting(project: Project) {
    if (project.isDisposed) return

    ApplicationManager.getApplication().invokeLater {
      if (project.isDisposed) return@invokeLater

      project.messageBus
        .syncPublisher(EVENT_TOPIC)
        .onDispatch(
          UserEvent(
            UserEvents.STARTUP,
            UserEventCategory.POSITIVE,
            PluginMessageBundle.message("user.event.startup.name"),
            project
          )
        )
    }
  }
}
