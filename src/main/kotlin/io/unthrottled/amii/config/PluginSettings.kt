package io.unthrottled.amii.config

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import java.net.URI

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var idleTimeOutInMinutes: Long,
  var notificationAnchorValue: String,
  var notificationModeValue: String,
)

object PluginSettings {

  const val PLUGIN_SETTINGS_DISPLAY_NAME = "AMII Settings"
  val CHANGELOG_URI =
    URI("https://github.com/Unthrottled/AMII/blob/master/CHANGELOG.md")
  private const val REPOSITORY = "https://github.com/Unthrottled/AMII"
  val ISSUES_URI = URI("$REPOSITORY/issues")

  private val initialPluginSettingsModel = getInitialConfigSettingsModel()

  @JvmStatic
  fun getInitialConfigSettingsModel() = ConfigSettingsModel(
    Config.instance.allowedExitCodes,
    Config.instance.idleTimeoutInMinutes,
    Config.instance.notificationAnchorValue,
    Config.instance.notificationModeValue,
  )

  private val pluginSettingsModel = initialPluginSettingsModel.copy()

//  override fun isModified(): Boolean {
//    return initialPluginSettingsModel != pluginSettingsModel
//  }

  @Suppress("LongMethod")
  private fun createSettingsPane(): DialogPanel {
//    val anchorPanel = getAnchorPositionPanel(
//      NotificationAnchor.fromValue(pluginSettingsModel.notificationAnchorValue)
//    ) {
//      pluginSettingsModel.notificationAnchorValue = it.toString()
//    }

    return panel {
    }
  }
}
