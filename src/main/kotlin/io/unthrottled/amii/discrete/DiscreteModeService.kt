package io.unthrottled.amii.discrete

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

fun Project.discreteModeService(): DiscreteModeService =
  this.getService(DiscreteModeService::class.java)

class DiscreteModeService(private val project: Project) : Disposable, Logging {
  private val connection = ApplicationManager.getApplication().messageBus.connect()
  private var currentMode = Config.instance.isDiscreteMode.toDiscreteMode()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        val nextDiscreteMode = newPluginState.isDiscreteMode.toDiscreteMode()
        if (nextDiscreteMode != currentMode) {
          currentMode = nextDiscreteMode;
          when (nextDiscreteMode) {
            DiscreteMode.ACTIVE -> applyDiscreteMode()
            DiscreteMode.INACTIVE -> liftDiscreteMode()
          }
        }
      }
    )
  }

  val isDiscreteMode: Boolean
    get() = Config.instance.isDiscreteMode

  override fun dispose() {
    connection.dispose()
  }

  private val gson = Gson()

  fun applyDiscreteMode() {
    project.memeService().clearMemes()
    Config.instance.discreteModeConfig =
      gson.toJson(captureRestorationConfig())
    Config.instance.showMood = false
    Config.instance.isDiscreteMode = true
    publishChanges()
  }

  fun liftDiscreteMode() {
    val discreteModeConfig = Config.instance.discreteModeConfig
    val restorationConfig = runSafelyWithResult({
      gson.fromJson(
        discreteModeConfig,
        object : TypeToken<DiscreteModeRestorationConfig>() {}.type
      )
    }) {
      logger().warn("Unable to read audible Assets for reasons $discreteModeConfig", it)
      captureRestorationConfig()
    }
    Config.instance.discreteModeConfig = "{}"
    Config.instance.showMood = restorationConfig.statusBarWidgetEnabled ?: true
    Config.instance.isDiscreteMode = false
    publishChanges()
  }

  private fun publishChanges() {
    ApplicationManager.getApplication().messageBus.syncPublisher(CONFIG_TOPIC)
      .pluginConfigUpdated(Config.instance)
  }

  private fun captureRestorationConfig() = DiscreteModeRestorationConfig(Config.instance.showMood)
}

data class DiscreteModeRestorationConfig(
  val statusBarWidgetEnabled: Boolean?
)
