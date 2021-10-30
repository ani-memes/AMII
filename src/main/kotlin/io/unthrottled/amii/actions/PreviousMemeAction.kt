package io.unthrottled.amii.actions

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.AlarmDebouncer
import io.unthrottled.amii.tools.Logging

class PreviousMemeAction : AnAction(), DumbAware, Logging, Disposable {

  companion object {
    private const val DEMAND_DELAY = 250
  }

  private val debouncer = AlarmDebouncer<AnActionEvent>(DEMAND_DELAY)

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project!!
    debouncer.debounce {
        project.memeService().displayLastMeme()
    }
  }

  override fun dispose() {
  }
}
