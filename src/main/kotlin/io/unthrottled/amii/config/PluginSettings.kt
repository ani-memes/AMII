package io.unthrottled.amii.config

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.panel
import io.unthrottled.amii.config.ui.AnchorPanelFactory.getAnchorPositionPanel
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.memes.PanelDismissalOptions
import java.net.URI
import javax.swing.JComponent

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var idleTimeOutInMinutes: Long,
  var notificationAnchorValue: String,
  var notificationModeValue: String,
)

class PluginSettings : SearchableConfigurable, DumbAware {

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
    Config.instance.notificationAnchorValue,
    Config.instance.notificationModeValue,
  )

  private val pluginSettingsModel = initialPluginSettingsModel.copy()

  override fun isModified(): Boolean {
    return initialPluginSettingsModel != pluginSettingsModel
  }

  override fun apply() {
    Config.instance.notificationAnchorValue = pluginSettingsModel.notificationAnchorValue
    Config.instance.notificationModeValue = pluginSettingsModel.notificationModeValue
    ApplicationManager.getApplication().messageBus.syncPublisher(
      CONFIG_TOPIC
    ).pluginConfigUpdated(Config.instance)
  }

  override fun createComponent(): JComponent {
    val tabbedPanel = JBTabbedPane()
    tabbedPanel.add("Main", createSettingsPane())
    return tabbedPanel
  }

  @Suppress("LongMethod")
  private fun createSettingsPane(): DialogPanel {
    val anchorPanel = getAnchorPositionPanel(
      NotificationAnchor.fromValue(pluginSettingsModel.notificationAnchorValue)
    ) {
      pluginSettingsModel.notificationAnchorValue = it.toString()
    }

    return panel {
      titledRow("Notification Settings") {
        row {
          // todo :layout
          buttonGroup("Dismissal") {
            row {
              radioButton(
                "Focus Loss",
                { pluginSettingsModel.notificationModeValue == PanelDismissalOptions.FOCUS_LOSS.toString() },
                {
                  if (it) {
                    pluginSettingsModel.notificationModeValue = PanelDismissalOptions.FOCUS_LOSS.toString()
                  }
                }
              )
            }
            row {
              radioButton(
                "Timed",
                { pluginSettingsModel.notificationModeValue == PanelDismissalOptions.TIMED.toString() },
                {
                  if (it) {
                    pluginSettingsModel.notificationModeValue = PanelDismissalOptions.TIMED.toString()
                  }
                }
              )
            }
          }
          buttonGroup {
            row { label("Anchoring") }
            row { anchorPanel() }
          }
        }
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
  }
}
