package io.unthrottled.amii.discrete

import com.intellij.openapi.project.Project

class ProjectDiscreteModeListener(private val project: Project) : DiscreteModeListener {
  override fun modeChanged(discreteMode: DiscreteMode) {
    when (discreteMode) {
      DiscreteMode.ACTIVE -> project.discreteModeService().applyDiscreteMode()
      DiscreteMode.INACTIVE -> project.discreteModeService().liftDiscreteMode()
    }
  }
}
