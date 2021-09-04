package io.unthrottled.amii.discreet

import com.intellij.openapi.project.Project

class ProjectDiscreetModeListener(private val project: Project) : DiscreetModeListener {
  override fun modeChanged(discreetMode: DiscreetMode) {
    when (discreetMode) {
      DiscreetMode.ACTIVE -> project.discreetModeService().applyDiscreetMode()
      DiscreetMode.INACTIVE -> project.discreetModeService().liftDiscreetMode()
    }
  }
}
