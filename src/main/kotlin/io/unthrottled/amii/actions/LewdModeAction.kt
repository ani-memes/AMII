package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.assets.LocalVisualContentManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.PluginMessageBundle

class LewdModeAction : BaseToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean =
    Config.instance.allowLewds

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    Config.instance.allowLewds = state
    ApplicationManager.getApplication().executeOnPooledThread {
      LocalVisualContentManager.rescanDirectory()
      if (state) {
        UpdateNotification.sendMessage(
          PluginMessageBundle.message("amii.lewd.asset.assets.enabled.title"),
          PluginMessageBundle.message("amii.lewd.asset.assets.enabled.body"),
          e.project
        )
      } else {
        UpdateNotification.sendMessage(
          PluginMessageBundle.message("amii.lewd.asset.assets.disabled.title"),
          PluginMessageBundle.message("amii.lewd.asset.assets.disabled.body"),
          e.project
        )
      }
    }
  }
}
