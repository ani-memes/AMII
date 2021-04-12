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
import io.unthrottled.amii.assets.ContentAssetManager.FALLBACK_ASSET_SOURCE
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
  val promotionAssetURL: String

  init {
    pluginLogoURL = getPluginLogo()
    promotionAssetURL = getPromotionAsset()
  }

  private fun getPluginLogo(): String = ContentAssetManager.resolveAssetUrl(
    AssetCategory.PROMOTION,
    "amii/logo.png"
  ).map { it.toString() }
    .orElse("$assetSource/promotion/amii/logo.png")

  private fun getPromotionAsset(): String =
    ContentAssetManager.resolveAssetUrl(AssetCategory.PROMOTION, "motivator/promotion.gif")
      .map { it.toString() }
      .orElse("$FALLBACK_ASSET_SOURCE/promotion/motivator/promotion.gif")
}

@Suppress("MaxLineLength")
class AniMemePromotionDialog(
  private val promotionAssets: PromotionAssets,
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
          <title>Motivator</title>
      </head>
      <body>
      <div class='logo-container'><img src="$pluginLogoURL" class='display-image' alt='Ani-Meme Plugin Logo'/>
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
    val promotionAssetURL = promotionAssets.promotionAssetURL

    @Language("HTML")
    val html =
      """
            <h2 class='header'>Your new virtual companion!</h2>
      <div style='margin: 8px 0 0 100px'>
        <p>
          <a href='https://plugins.jetbrains.com/plugin/15865-amii'>The Anime Meme plugin</a>
          gives your IDE more personality by using anime memes. <br/> You will get an assistant that will interact
          with you as you build code.
          <br/>Such as when your programs fail to run or tests pass/fail. Your companion<br/>
          has the ability to react to these events. Which will most likely take the form <br/> of a reaction gif of
          your favorite character(s)!
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>Bring Anime Memes to your IDE today!</h3>
      <div class='display-image'><img src='$promotionAssetURL' height="150"/></div>
      <br/>
      """.trimIndent()
    return html
  }

  private fun existingUserPromotion(): String {
    @Language("HTML")
    val html =
      """
            <h2 class='header'>A brand new experience!</h2>
      <div style='margin: 8px 0 0 100px'>
        <p>
          As of Waifu Motivator v2.0, notifications have been moved to
<a href='https://plugins.jetbrains.com/plugin/15865-amii'>the Anime Meme</a> plugin. <br><br>
          <div>Whats better?<br/>
            <ul>
                <li>More Content!</li>
                <li>More Customization!</li>
            </ul>
          Breaking Changes:
            <ul>
                <li>Removed titled notifications.</li>
                <li>Your previous configurations will be lost (Sorry!).</li>
            </ul>
            </div>
          For a list of more breaking changes and enhancements please see
          <a href="https://github.com/waifu-motivator/waifu-motivator-plugin/blob/master/docs/CHANGELOG.md">the changelog</a>
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>I hope you enjoy!</h3>
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
