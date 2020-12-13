package io.unthrottled.amii.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.Constants.PLUGIN_ID
import io.unthrottled.amii.platform.UpdateAssetsListener
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import java.util.UUID

object UserOnBoarding {

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
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != Config.instance.version }

  private fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
