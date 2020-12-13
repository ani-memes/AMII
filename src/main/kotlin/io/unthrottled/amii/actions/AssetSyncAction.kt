package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.platform.UpdateAssetsListener
import io.unthrottled.amii.tools.PluginMessageBundle

class AssetSyncAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(UpdateAssetsListener.TOPIC)
      .onRequestedUpdate()
    UpdateNotification.sendMessage(
      PluginMessageBundle.message("actions.sync.title"),
      PluginMessageBundle.message("actions.sync.message"),
      e.project
    )
  }
}
