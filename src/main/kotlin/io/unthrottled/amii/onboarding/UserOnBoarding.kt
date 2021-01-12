package io.unthrottled.amii.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.Constants.PLUGIN_ID
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.platform.UpdateAssetsListener
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import java.util.UUID

object UserOnBoarding {

  private val addedEvents = setOf(
    UserEvents.SILENCE
  ).map { it.value }

  fun attemptToPerformNewUpdateActions(project: Project) {
    getNewVersion().ifPresent { newVersion ->
      Config.instance.version = newVersion
      ApplicationManager.getApplication().messageBus
        .syncPublisher(UpdateAssetsListener.TOPIC)
        .onRequestedUpdate()
      StartupManager.getInstance(project)
        .runWhenProjectIsInitialized {
          UpdateNotification.display(project, newVersion)
        }
    }

    if (Config.instance.userId.isEmpty()) {
      Config.instance.userId = UUID.randomUUID().toString()
    }

    // Add new events for user
    Config.instance.enabledEvents = addedEvents.stream()
      .reduce(Config.instance.enabledEvents) { accum, newEventToAdd ->
        accum or newEventToAdd
      }
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != Config.instance.version }

  fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
