package io.unthrottled.amii.personality

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import io.unthrottled.amii.config.Config

class MoodStatusBarProvider : StatusBarWidgetFactory {
  companion object {
    private const val ID = "io.unthrottled.amii.personality.MoodStatusBarProvider"
  }

  override fun getId(): String = ID

  override fun getDisplayName(): String =
    "Waifu Mood Display"

  override fun disposeWidget(widget: StatusBarWidget) {
    widget.dispose()
  }

  override fun isAvailable(project: Project): Boolean =
    true

  override fun createWidget(project: Project): StatusBarWidget =
    MoodStatusBarWidget(project)

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean =
    true

  override fun isConfigurable(): Boolean = true

  override fun isEnabledByDefault(): Boolean =
    Config.instance.showMood
}
