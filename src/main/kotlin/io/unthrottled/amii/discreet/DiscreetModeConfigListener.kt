package io.unthrottled.amii.discreet

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener

class DiscreetModeConfigListener : ConfigListener {
  private var currentMode = Config.instance.discreetMode.toDiscreetMode()
  override fun pluginConfigUpdated(config: Config) {
    val discreetMode = config.discreetMode.toDiscreetMode()
    if (discreetMode != currentMode) {
      ApplicationManager.getApplication().messageBus.syncPublisher(DiscreetModeListener.DISCREET_MODE_TOPIC)
        .modeChanged(discreetMode)
    }
  }
}
