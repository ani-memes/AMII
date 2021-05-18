package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.memes.memeService

class ClearAction : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    e.project?.memeService()?.clearMemes()
  }
}
