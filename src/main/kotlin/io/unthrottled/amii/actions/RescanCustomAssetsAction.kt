package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.assets.LocalContentManager
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.PluginMessageBundle

class RescanCustomAssetsAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    LocalContentManager.refreshStuff {
      UpdateNotification.sendMessage(
        PluginMessageBundle.message("amii.local.asset.sync.done.title"),
        PluginMessageBundle.message("amii.local.asset.sync.done.body"),
        e.project
      )
    }
  }
}
