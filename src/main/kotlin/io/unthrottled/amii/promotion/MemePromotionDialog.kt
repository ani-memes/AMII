package io.unthrottled.amii.promotion

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser
import com.intellij.ui.JBColor
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.PluginMessageBundle
import java.awt.Dimension
import java.awt.Window
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JTextPane
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
) : DialogWrapper(parent, true) {

  companion object {
    private const val INSTALLED_EXIT_CODE = 69
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
        PluginsAdvertiser.installAndEnable(
          setOf(
            PluginId.getId(promotionDefinition.pluginId)
          )
        ) {
          close(INSTALLED_EXIT_CODE, true)
        }
      }
    }
  }

  override fun createCenterPanel(): JComponent? {
    val promotionPane = buildPromotionPane()
    return panel {
      row {
        promotionPane()
      }
    }
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
) : DialogWrapper.DoNotAskOption {
  override fun isToBeShown(): Boolean = true

  override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
    onToBeShown(toBeShown, exitCode)
  }

  override fun canBeHidden(): Boolean = true

  override fun shouldSaveOptionsOnCancel(): Boolean = true

  override fun getDoNotShowMessage(): String =
    PluginMessageBundle.message("promotions.dont.ask")
}