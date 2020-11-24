package io.unthrottled.amii.config

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.panel
import io.unthrottled.amii.config.ui.AnchorPanelFactory.getAnchorPositionPanel
import io.unthrottled.amii.config.ui.NotificationAnchor
import java.net.URI
import java.util.Vector
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var idleTimeOutInMinutes: Long,
  var notificationAnchor: String,
  var notificationMode: String,
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
    Config.instance.notificationAnchor,
    Config.instance.notificationMode,
  )

  private val pluginSettingsModel = initialPluginSettingsModel.copy()

  override fun isModified(): Boolean {
    return initialPluginSettingsModel != pluginSettingsModel
  }

  override fun apply() {
    Config.instance.notificationAnchor = pluginSettingsModel.notificationAnchor
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
      NotificationAnchor.fromValue(pluginSettingsModel.notificationAnchor)
    ) {
      pluginSettingsModel.notificationAnchor = it.toString()
    }

    val notifcationModeComboBox = ComboBox(
      DefaultComboBoxModel(
        Vector(
          listOf("Click To Dismiss", "Duration")
        )
      )
    )
    notifcationModeComboBox.model.selectedItem = pluginSettingsModel.notificationMode
    notifcationModeComboBox.addActionListener {
      pluginSettingsModel.notificationMode = notifcationModeComboBox.model.selectedItem as String
    }

    return panel {
      titledRow("Notification Settings") {
        row {
          // todo: add listeners & layout
          buttonGroup("Dismissal") {
            row { radioButton("Focus Loss Dismiss") }
            row { radioButton("Timed Dismiss") }
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
