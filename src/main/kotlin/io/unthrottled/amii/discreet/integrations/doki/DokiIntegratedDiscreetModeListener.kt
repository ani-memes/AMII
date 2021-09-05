package io.unthrottled.amii.discreet.integrations.doki

import io.unthrottled.amii.discreet.AbstractDiscreetModeListener
import io.unthrottled.doki.discreet.DiscreetMode
import io.unthrottled.doki.discreet.IntegratedDiscreetModeListener

class DokiIntegratedDiscreetModeListener :
  IntegratedDiscreetModeListener,
  AbstractDiscreetModeListener() {

  override fun dispatchExtraEvents() {
  }

  override fun modeChanged(discreetMode: DiscreetMode) {
    this.modeChanged(
      when (discreetMode) {
        DiscreetMode.ACTIVE -> io.unthrottled.amii.discreet.DiscreetMode.ACTIVE
        DiscreetMode.INACTIVE -> io.unthrottled.amii.discreet.DiscreetMode.INACTIVE
      }
    )
  }
}
