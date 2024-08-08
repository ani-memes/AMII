package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

abstract class BaseToggleAction : ToggleAction(), DumbAware {
  override fun update(e: AnActionEvent) {
    // Not calling super enables the icons to be shown :D
  }

  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.BGT
}
