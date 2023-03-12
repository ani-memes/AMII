package io.unthrottled.amii.promotion

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DoNotAskOption
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.installAndEnable
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafely
import java.awt.Dimension
import java.awt.Window
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class PromotionAssets(
  val isNewUser: Boolean,
  private val promotionDefinition: PromotionDefinition,
) {

  val pluginLogoURL: String

  init {
    pluginLogoURL = getPluginLogo()
  }

  private fun getPluginLogo(): String = promotionDefinition.getLogoUrl()
}

@Suppress("MaxLineLength")
class AniMemePromotionDialog(
  private val promotionAssets: PromotionAssets,
  private val promotionDefinition: PromotionDefinition,
  parent: Window,
  private val onPromotion: (PromotionResults) -> Unit
) : DialogWrapper(parent, true), Logging {

  companion object {
    private const val INSTALLED_EXIT_CODE = 69
    private const val ERROR_EXIT_CODE = -1
    private const val EXTRA_WINDOW_PADDING = 120
  }

  init {
    title = promotionDefinition.dialogTitle
    setCancelButtonText(PluginMessageBundle.message("promotion.action.cancel"))
    setDoNotAskOption(
      DoNotPromote { shouldContinuePromotion, exitCode ->
        onPromotion(
          PromotionResults(
            when {
              !shouldContinuePromotion -> PromotionStatus.BLOCKED
              exitCode == INSTALLED_EXIT_CODE -> PromotionStatus.ACCEPTED
              exitCode == ERROR_EXIT_CODE -> PromotionStatus.ERROR
              else -> PromotionStatus.REJECTED
            }
          )
        )
      }
    )
    init()
  }

  override fun createActions(): Array<Action> {
    return arrayOf(
      buildInstallAction(),
      cancelAction
    )
  }

  private fun buildInstallAction(): AbstractAction {
    return object : AbstractAction() {
      init {
        val message = PluginMessageBundle.message("promotion.action.ok")
        putValue(NAME, message)
        putValue(SMALL_ICON, promotionDefinition.icon)
      }

      override fun actionPerformed(e: ActionEvent) {
        val pluginIds = setOf(
          PluginId.getId(promotionDefinition.pluginId)
        )
        val onSuccess = Runnable {
          close(INSTALLED_EXIT_CODE, true)
        }
        runSafely({
          installAndEnable(pluginIds, onSuccess)
        }) { installError ->
          logger().warn("Unable to install and enable, trying hax", installError)
          runSafely({
            val pluginAdvertiser =
              Class.forName("com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser")
            val installAndEnable = pluginAdvertiser.declaredMethods
              .first { it.name == "installAndEnable" && it.parameterCount == 1 }
            installAndEnable?.invoke(null, pluginIds, onSuccess)
          }) {
            logger().warn("Unable to try hax with install and enable", it)
            UpdateNotification.sendMessage(
              PluginMessageBundle.message("promotion.unable.to.install.title"),
              PluginMessageBundle.message("promotion.unable.to.install.message")
            )
            close(ERROR_EXIT_CODE, false)
          }
        }
      }
    }
  }

  override fun createCenterPanel(): JComponent {
    return buildPromotionPane()
  }

  @Suppress("LongMethod")
  private fun buildPromotionPane(): JEditorPane {
    val pane = JTextPane()
    pane.isEditable = false
    pane.contentType = "text/html"
    pane.background = JBColor.namedColor(
      "Menu.background",
      UIUtil.getEditorPaneBackground()
    )

    pane.text = promotionDefinition.getPromotionBody(promotionAssets)
    pane.preferredSize = Dimension(
      pane.preferredSize.width + EXTRA_WINDOW_PADDING,
      pane.preferredSize.height
    )
    pane.addHyperlinkListener {
      if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        BrowserUtil.browse(it.url)
      }
    }
    return pane
  }
}

class DoNotPromote(
  private val onToBeShown: (Boolean, Int) -> Unit
) : DoNotAskOption {
  override fun isToBeShown(): Boolean = true

  override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
    onToBeShown(toBeShown, exitCode)
  }

  override fun canBeHidden(): Boolean = true

  override fun shouldSaveOptionsOnCancel(): Boolean = true

  override fun getDoNotShowMessage(): String =
    PluginMessageBundle.message("promotions.dont.ask")
}
