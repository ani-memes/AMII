package io.unthrottled.amii.discreet

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult

class ApplicationDiscreetModeListener : DiscreetModeListener, Logging {
  private val gson = Gson()
  private var currentMode = Config.instance.discreetMode.toDiscreetMode()

  override fun modeChanged(discreetMode: DiscreetMode) {
    if (discreetMode != currentMode) {
      if (discreetMode == DiscreetMode.ACTIVE) {
        applyDiscreetMode()
      } else {
        liftDiscreetMode()
      }
    }
  }

  private fun liftDiscreetMode() {
    if (currentMode == DiscreetMode.INACTIVE) return
    currentMode = DiscreetMode.INACTIVE
    val discreetModeConfig = Config.instance.discreetModeConfig
    val restorationConfig = runSafelyWithResult({
      gson.fromJson(
        discreetModeConfig,
        object : TypeToken<DiscreetModeRestorationConfig>() {}.type
      )
    }) {
      logger().warn("Unable to read discreet mode restoration config $discreetModeConfig", it)
      captureRestorationConfig()
    }
    Config.instance.discreetModeConfig = "{}"
    Config.instance.showMood = restorationConfig.statusBarWidgetEnabled ?: true
    Config.instance.discreetMode = false
    publishChanges()
  }

  private fun applyDiscreetMode() {
    if (currentMode == DiscreetMode.ACTIVE) return
    currentMode = DiscreetMode.ACTIVE
    val restorationConfig = gson.toJson(captureRestorationConfig())
    Config.instance.discreetModeConfig = restorationConfig
    Config.instance.showMood = false
    Config.instance.discreetMode = true
    publishChanges()
  }

  private fun publishChanges() {
    ApplicationManager.getApplication().messageBus.syncPublisher(ConfigListener.CONFIG_TOPIC)
      .pluginConfigUpdated(Config.instance)

    ProjectManager.getInstance().openProjects.forEach {
      it.messageBus.syncPublisher(DiscreetModeListener.DISCREET_MODE_TOPIC)
        .modeChanged(currentMode)
    }
  }

  private fun captureRestorationConfig() = DiscreetModeRestorationConfig(Config.instance.showMood)
}

data class DiscreetModeRestorationConfig(
  val statusBarWidgetEnabled: Boolean?
)
