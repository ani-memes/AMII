package io.unthrottled.amii.config

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.panel
import java.net.URI
import javax.swing.JComponent

data class ConfigSettingsModel(
  val allowedExitCodes: String,
  val idleTimeOutInMinutes: Long,
)

class PluginSettings : SearchableConfigurable {

  companion object {
    const val PLUGIN_SETTINGS_DISPLAY_NAME = "AMII Settings"
    val CHANGELOG_URI =
      URI("https://github.com/Unthrottled/AMII/blob/master/CHANGELOG.md")
    private const val REPOSITORY = "https://github.com/Unthrottled/AMII"
    val ISSUES_URI = URI("$REPOSITORY/issues")
  }

  override fun getId(): String = "io.unthrottled.amii.config.PluginSettings"

  override fun getDisplayName(): String =
    PLUGIN_SETTINGS_DISPLAY_NAME

  private val initialPluginSettingsModel = ConfigSettingsModel(
    Config.instance.allowedExitCodes,
    Config.instance.idleTimeoutInMinutes,
  )

  private val pluginSettingsModel = initialPluginSettingsModel.copy()

  override fun isModified(): Boolean {
    return initialPluginSettingsModel != pluginSettingsModel
  }

  override fun apply() {
//    MaterialIconsActor.enablePSIIcons(themeSettingsModel.isMaterialPSIIcons)
    ApplicationManager.getApplication().messageBus.syncPublisher(
      CONFIG_TOPIC
    ).pluginConfigUpdated(Config.instance)
  }

  override fun createComponent(): JComponent? {
    val tabbedPanel = JBTabbedPane()
    tabbedPanel.add("Main", createSettingsPane())
    return tabbedPanel
  }

  @Suppress("LongMethod")
  private fun createSettingsPane(): DialogPanel {
    val settingsPane = panel {
      titledRow("Main Settings") {
//        row {
//          checkBox(
//            "Enable Stickers",
//            pluginSettingsModel.areStickersEnabled,
//            actionListener = { _, component ->
//              pluginSettingsModel.areStickersEnabled = component.isSelected
//            }
//          )
//        }
      }
      titledRow("Miscellaneous Items") {
        row {
          cell {
            button("View Issues") {
              BrowserUtil.browse(ISSUES_URI)
            }
            button("View Changelog") {
              BrowserUtil.browse(CHANGELOG_URI)
            }
          }
        }
      }
    }
    return settingsPane
  }
}
