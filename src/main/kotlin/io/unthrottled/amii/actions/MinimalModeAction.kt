package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.PluginMessageBundle

class MinimalModeAction : ToggleAction(), DumbAware {
  override fun isSelected(e: AnActionEvent): Boolean =
    Config.instance.minimalMode

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    Config.instance.minimalMode = state
    ApplicationManager.getApplication().messageBus.syncPublisher(
      ConfigListener.CONFIG_TOPIC
    ).pluginConfigUpdated(Config.instance)
    if (state) {
      UpdateNotification.sendMessage(
        PluginMessageBundle.message("actions.minimal.enabled.title"),
        PluginMessageBundle.message("actions.minimal.enabled.message"),
        e.project
      )
    } else {
      UpdateNotification.sendMessage(
        PluginMessageBundle.message("actions.minimal.disabled.title"),
        PluginMessageBundle.message("actions.minimal.disabled.message"),
        e.project
      )
    }
  }
}
