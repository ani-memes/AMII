package io.unthrottled.amii.promotion

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser
import com.intellij.ui.JBColor
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import icons.AMIIIcons
import io.unthrottled.amii.assets.AssetCategory
import io.unthrottled.amii.assets.ContentAssetManager
import io.unthrottled.amii.assets.ContentAssetManager.assetSource
import io.unthrottled.amii.config.Constants
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.toHexString
import org.intellij.lang.annotations.Language
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
  val isNewUser: Boolean
) {

  val pluginLogoURL: String

  init {
    pluginLogoURL = getPluginLogo()
  }

  private fun getPluginLogo(): String = ContentAssetManager.resolveAssetUrl(
    AssetCategory.PROMOTION,
    "amii/amii_rider_extension_logo.png",
  ).map { it.toString() }
    .orElse("$assetSource/promotion/amii/amii_rider_extension_logo.png")
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
    title = PluginMessageBundle.message("amii.rider.extension.name")
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
        putValue(SMALL_ICON, AMIIIcons.Plugins.Rider.AMII)
      }

      override fun actionPerformed(e: ActionEvent) {
        PluginsAdvertiser.installAndEnable(
          setOf(
            PluginId.getId(Constants.RIDER_EXTENSION_ID)
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
    val accentHex = JBColor.namedColor(
      "Link.activeForeground",
      UIUtil.getTextAreaForeground()
    ).toHexString()
    val infoForegroundHex = UIUtil.getContextHelpForeground().toHexString()
    val pluginLogoURL = promotionAssets.pluginLogoURL
    pane.background = JBColor.namedColor(
      "Menu.background",
      UIUtil.getEditorPaneBackground()
    )

    pane.text =
      """
      <html lang="en">
      <head>
          <style type='text/css'>
              body {
                font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
              }
              .center {
                text-align: center;
              }
              a {
                  color: $accentHex;
                  font-weight: bold;
              }
              p, div, ul, li {
                color: ${UIUtil.getLabelForeground().toHexString()};
              }
              h2 {
                margin: 16px 0;
                font-weight: bold;
                font-size: 22px;
              }
              h3 {
                margin: 4px 0;
                font-weight: bold;
                font-size: 14px;
              }
              .accented {
                color: $accentHex;
              }
              .info-foreground {
                color: $infoForegroundHex;
                text-align: center;
              }
              .header {
                color: $accentHex;
                text-align: center;
              }
              .logo-container {
                margin-top: 8px;
                text-align: center;
              }
              .display-image {
                max-height: 256px;
                text-align: center;
              }
          </style>
      </head>
      <body>
      <div class='logo-container'><img src="$pluginLogoURL" class='display-image'
       style="max-height: 256px" alt='Ani-Meme Rider Extension Logo'/>
      </div>
      ${getPromotionContent(promotionAssets.isNewUser)}
      <br/>
      </body>
      </html>
      """.trimIndent()
    pane.preferredSize = Dimension(pane.preferredSize.width + EXTRA_WINDOW_PADDING, pane.preferredSize.height)
    pane.addHyperlinkListener {
      if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        BrowserUtil.browse(it.url)
      }
    }
    return pane
  }

  private fun getPromotionContent(newUser: Boolean): String {
    return if (newUser) newUserPromotion()
    else existingUserPromotion()
  }

  private fun newUserPromotion(): String {
    @Language("HTML")
    val html =
      """
            <h2 class='header'>Extra Attention Required!</h2>
      <div style='margin: 8px 0 0 100px'>
        <p>
          The Rider IDE is a special platform that requires extra love <br>
          and attention to get AMII to work. <a href='https://plugins.jetbrains.com/plugin/16518-anime-memes--rider-extension'>The Anime Meme Ride Extension</a>
          enables <br>
          full functionality of the <a href='https://github.com/ani-memes/AMII'>Anime Meme plugin</a> on the Rider Platform.
          Don't miss out <br/> on any of the important features supplied by AMII!
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>Get the complete experience!</h3>
      <br/>
      """.trimIndent()
    return html
  }

  private fun existingUserPromotion(): String {
    @Language("HTML")
    val html =
      """
            <h2 class='header'>You're Missing Out!</h2>
      <div style='margin: 8px 0 0 100px'>
        <p>
          I learned that the Rider IDE is a special platform that requires extra love <br>
        and attention to get AMII to work. <a href='https://plugins.jetbrains.com/plugin/16518-anime-memes--rider-extension'>The Anime Meme Ride Extension</a>
        enables <br>
        full functionality of the <a href='https://plugins.jetbrains.com/plugin/15865-amii'>the Anime Meme</a> plugin. <br><br>
          <div>What's provided by the extension<br/>
            <ul>
                <li>Unit Test Reactions.</li>
                <li>Build Task Interactions.</li>
            </ul>
            </div>
          For a list of more features provided and enhancements <br> please see
          <a href="https://github.com/ani-memes/amii-rider-extension#features">the documentation.</a>
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>Get the complete experience!</h3>
      <br/>
      """.trimIndent()
    return html
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
