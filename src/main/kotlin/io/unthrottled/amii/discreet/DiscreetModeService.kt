package io.unthrottled.amii.discreet

import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.Logging

fun Project.discreetModeService(): DiscreetModeService =
  this.getService(DiscreetModeService::class.java)

class DiscreetModeService(private val project: Project) : Logging {
  private var currentMode = Config.instance.discreetMode.toDiscreetMode()

  val isDiscreetMode: Boolean
    get() = Config.instance.discreetMode

  fun applyDiscreetMode() {
    if (currentMode == DiscreetMode.ACTIVE) return
    currentMode = DiscreetMode.ACTIVE
    project.memeService().clearMemes()
  }

  fun liftDiscreetMode() {
    if (currentMode == DiscreetMode.INACTIVE) return
    currentMode = DiscreetMode.INACTIVE
  }

}
