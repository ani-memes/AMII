package io.unthrottled.amii.promotion

import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import icons.AMIIIcons
import io.unthrottled.amii.assets.AssetCategory
import io.unthrottled.amii.assets.ContentAssetManager
import io.unthrottled.amii.config.Constants
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.toHexString
import org.intellij.lang.annotations.Language
import java.util.UUID
import javax.swing.Icon

abstract class PromotionDefinition(
  val id: UUID,
  val dialogTitle: String,
  val icon: Icon,
  val pluginId: String,
) {

  abstract fun shouldInstall(): Boolean

  abstract fun getLogoUrl(): String

  abstract fun getPromotionBody(promotionAssets: PromotionAssets): String
}

@Suppress("MaxLineLength", "LongMethod")
val riderPromotion = object : PromotionDefinition(
  UUID.fromString("ebd20408-f174-4fb0-bdd8-6bf81e3b5a1b"),
  PluginMessageBundle.message("amii.rider.extension.name"),
  AMIIIcons.Plugins.Rider.AMII,
  Constants.RIDER_EXTENSION_ID,
) {

  override fun shouldInstall(): Boolean = AppService.isRiderPlatform() &&
    PluginService.isRiderExtensionInstalled().not() &&
    PluginService.canRiderExtensionBeInstalled()

  override fun getLogoUrl(): String =
    ContentAssetManager.resolveAssetUrl(
      AssetCategory.PROMOTION,
      "amii/amii_rider_extension_logo.png",
    ).map { it.toString() }
      .orElse("${ContentAssetManager.assetSource}/promotion/amii/amii_rider_extension_logo.png")

  override fun getPromotionBody(promotionAssets: PromotionAssets): String {
    val accentHex = JBColor.namedColor(
      "Link.activeForeground",
      UIUtil.getTextAreaForeground()
    ).toHexString()
    val infoForegroundHex = UIUtil.getContextHelpForeground().toHexString()
    val pluginLogoURL = promotionAssets.pluginLogoURL

    @Language("HTML")
    val html = """
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

    return html
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

@Suppress("MaxLineLength", "LongMethod")
val androidPromotion = object : PromotionDefinition(
  UUID.fromString("99409eff-a291-4e04-974b-13bf4939d9d4"),
  PluginMessageBundle.message("amii.android.extension.name"),
  AMIIIcons.Plugins.Android.AMII,
  Constants.ANDROID_EXTENSION_ID,
) {

  override fun shouldInstall(): Boolean = AppService.isAndroidStudio() &&
    PluginService.isAndroidExtensionInstalled().not() &&
    PluginService.canAndroidExtensionBeInstalled()

  override fun getLogoUrl(): String =
    ContentAssetManager.resolveAssetUrl(
      AssetCategory.PROMOTION,
      "amii/amii_android_extension_logo.png",
    ).map { it.toString() }
      .orElse("${ContentAssetManager.assetSource}/promotion/amii/amii_android_extension_logo.png")

  override fun getPromotionBody(promotionAssets: PromotionAssets): String {
    val accentHex = JBColor.namedColor(
      "Link.activeForeground",
      UIUtil.getTextAreaForeground()
    ).toHexString()
    val infoForegroundHex = UIUtil.getContextHelpForeground().toHexString()
    val pluginLogoURL = promotionAssets.pluginLogoURL

    @Language("HTML")
    val html = """
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
       style="max-height: 256px" alt='Ani-Meme Android Extension Logo'/>
      </div>
      ${getPromotionContent(promotionAssets.isNewUser)}
      <br/>
      </body>
      </html>
    """.trimIndent()

    return html
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
          Android Studio is a special platform that requires extra love <br>
          and attention to get AMII to work. <a href='https://plugins.jetbrains.com/plugin/16802-anime-memes--android-extension'>The Anime Meme Ride Extension</a>
          enables <br>
          full functionality of the <a href='https://github.com/ani-memes/AMII'>Anime Meme plugin</a> on the Android Studio Platform.
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
          I learned that the Android Studio is a special platform that requires extra love <br>
        and attention to get AMII to work. <a href='https://plugins.jetbrains.com/plugin/16802-anime-memes--android-extension'>The Anime Meme Android Extension</a>
        enables <br>
        full functionality of the <a href='https://plugins.jetbrains.com/plugin/15865-amii'>the Anime Meme</a> plugin. <br><br>
          <div>What's provided by the extension<br/>
            <ul>
                <li>Build Task Interactions.</li>
            </ul>
            </div>
          For a list of more features provided and enhancements <br> please see
          <a href="https://github.com/ani-memes/amii-android-extension#features">the documentation.</a>
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>Get the complete experience!</h3>
      <br/>
      """.trimIndent()
    return html
  }
}
