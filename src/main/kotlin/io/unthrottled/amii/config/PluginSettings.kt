package io.unthrottled.amii.config

import java.net.URI

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var idleTimeOutInMinutes: Long,
  var memeDisplayAnchorValue: String,
  var memeDisplayModeValue: String,
  var memeDisplayInvulnerabilityDuration: Int,
  var memeDisplayTimedDuration: Int,
  var memeVolume: Int,
  var soundEnabled: Boolean,
  var preferredGenders: Int,
  var allowFrustration: Boolean,
  var enabledEvents: Int,
  var logKeyword: String,
  var logSearchIgnoreCase: Boolean,
)

object PluginSettings {
  const val PLUGIN_SETTINGS_DISPLAY_NAME = "AMII Settings"
  val CHANGELOG_URI =
    URI("https://github.com/Unthrottled/AMII/blob/master/CHANGELOG.md")
  private const val REPOSITORY = "https://github.com/Unthrottled/AMII"
  val ISSUES_URI = URI("$REPOSITORY/issues")

  @JvmStatic
  fun getInitialConfigSettingsModel() = ConfigSettingsModel(
    Config.instance.allowedExitCodes,
    Config.instance.idleTimeoutInMinutes,
    Config.instance.memeDisplayAnchorValue,
    Config.instance.memeDisplayModeValue,
    Config.instance.memeDisplayInvulnerabilityDuration,
    Config.instance.memeDisplayTimedDuration,
    Config.instance.memeVolume,
    Config.instance.soundEnabled,
    Config.instance.preferredGenders,
    Config.instance.allowFrustration,
    Config.instance.enabledEvents,
    Config.instance.logSearchTerms,
    Config.instance.logSearchIgnoreCase,
  )
}
