package io.unthrottled.amii.discrete

import com.intellij.openapi.project.Project

class ProjectDiscreteModeListener(private val project: Project) : DiscreteModeListener {
  override fun modeChanged(discreteMode: DiscreteMode) {
    when (discreteMode) {
      DiscreteMode.ACTIVE -> project.getDiscreteModeService().applyDiscreteMode()
      DiscreteMode.INACTIVE -> project.getDiscreteModeService().liftDiscreteMode()
    }
  }
}
