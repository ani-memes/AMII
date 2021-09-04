package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import io.unthrottled.amii.discrete.getDiscreteModeService

class DiscreteModeAction : BaseToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean =
    e.project?.getDiscreteModeService()?.isDiscreteMode == true

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val discreteModeService = e.project?.getDiscreteModeService()
    if (state) {
      discreteModeService?.applyDiscreteMode()
    } else {
      discreteModeService?.liftDiscreteMode()
    }
  }
}
