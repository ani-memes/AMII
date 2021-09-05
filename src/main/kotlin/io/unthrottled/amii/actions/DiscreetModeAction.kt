package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.discreet.DiscreetModeListener
import io.unthrottled.amii.discreet.discreetModeService
import io.unthrottled.amii.discreet.toDiscreetMode

class DiscreetModeAction : BaseToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean =
    e.project?.discreetModeService()?.isDiscreetMode == true

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(DiscreetModeListener.DISCREET_MODE_TOPIC)
      .modeChanged(state.toDiscreetMode())
  }
}
