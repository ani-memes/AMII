package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import io.unthrottled.amii.discreet.discreetModeService

class DiscreetModeAction : BaseToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean =
    e.project?.discreetModeService()?.isDiscreetMode == true

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val discreetModeService = e.project?.discreetModeService()
    if (state) {
      discreetModeService?.applyDiscreetMode()
    } else {
      discreetModeService?.liftDiscreetMode()
    }
  }
}
