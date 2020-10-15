package io.unthrottled.amii.onboarding

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import io.unthrottled.amii.AMII.PLUGIN_ID
import io.unthrottled.amii.config.PluginConfig
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import java.util.UUID

object UserOnBoarding {

  fun attemptToPerformNewUpdateActions(project: Project) {
    getNewVersion().ifPresent { newVersion ->
      PluginConfig.instance.version = newVersion
      StartupManager.getInstance(project)
        .runWhenProjectIsInitialized {
          UpdateNotification.display(project, newVersion)
        }
    }

    if (PluginConfig.instance.userId.isEmpty()) {
      PluginConfig.instance.userId = UUID.randomUUID().toString()
    }
  }

  private fun getNewVersion() =
    getVersion()
      .filter { it != PluginConfig.instance.version }

  private fun getVersion(): Optional<String> =
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
      .toOptional()
      .map { it.version }
}
