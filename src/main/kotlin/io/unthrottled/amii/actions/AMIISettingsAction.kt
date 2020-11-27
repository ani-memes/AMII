package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.config.ui.PluginSettingsUI

class AMIISettingsAction : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    ShowSettingsUtil.getInstance()
      .showSettingsDialog(e.project, PluginSettingsUI::class.java)
  }
}
