package io.unthrottled.amii.discrete

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC

fun Project.getDiscreteModeService(): DiscreteModeService =
  this.getService(DiscreteModeService::class.java)

class DiscreteModeService : Disposable {
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

  fun applyDiscreteMode() {
    TODO("Not yet implemented")
  }

  fun liftDiscreteMode() {
    TODO("Not yet implemented")
  }
}
