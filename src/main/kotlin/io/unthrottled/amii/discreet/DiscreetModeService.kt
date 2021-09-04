package io.unthrottled.amii.discreet

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC
import io.unthrottled.amii.memes.memeService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult

fun Project.discreetModeService(): DiscreetModeService =
  this.getService(DiscreetModeService::class.java)

class DiscreetModeService(private val project: Project) : Disposable, Logging {
  private val connection = ApplicationManager.getApplication().messageBus.connect()
  private var currentMode = Config.instance.discreetMode.toDiscreetMode()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        val nextDiscreetMode = newPluginState.discreetMode.toDiscreetMode()
        if (nextDiscreetMode != currentMode) {
          currentMode = nextDiscreetMode;
          when (nextDiscreetMode) {
            DiscreetMode.ACTIVE -> applyDiscreetMode()
            DiscreetMode.INACTIVE -> liftDiscreetMode()
          }
        }
      }
    )
  }

  val isDiscreetMode: Boolean
    get() = Config.instance.discreetMode

  override fun dispose() {
    connection.dispose()
  }

  private val gson = Gson()

  fun applyDiscreetMode() {
    project.memeService().clearMemes()
    Config.instance.discreetModeConfig =
      gson.toJson(captureRestorationConfig())
    Config.instance.showMood = false
    Config.instance.discreetMode = true
    publishChanges()
  }

  fun liftDiscreetMode() {
    val discreetModeConfig = Config.instance.discreetModeConfig
    val restorationConfig = runSafelyWithResult({
      gson.fromJson(
        discreetModeConfig,
        object : TypeToken<DiscreetModeRestorationConfig>() {}.type
      )
    }) {
      logger().warn("Unable to read audible Assets for reasons $discreetModeConfig", it)
      captureRestorationConfig()
    }
    Config.instance.discreetModeConfig = "{}"
    Config.instance.showMood = restorationConfig.statusBarWidgetEnabled ?: true
    Config.instance.discreetMode = false
    publishChanges()
  }

  private fun publishChanges() {
    ApplicationManager.getApplication().messageBus.syncPublisher(CONFIG_TOPIC)
      .pluginConfigUpdated(Config.instance)
  }

  private fun captureRestorationConfig() = DiscreetModeRestorationConfig(Config.instance.showMood)
}

data class DiscreetModeRestorationConfig(
  val statusBarWidgetEnabled: Boolean?
)
