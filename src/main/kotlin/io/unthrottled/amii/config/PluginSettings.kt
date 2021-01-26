package io.unthrottled.amii.config

import java.net.URI

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var idleTimeoutInMinutes: Long,
  var silenceTimeoutInMinutes: Long,
  var memeDisplayAnchorValue: String,
  var idleMemeDisplayAnchorValue: String,
  var memeDisplayModeValue: String,
  var memeDisplayInvulnerabilityDuration: Int,
  var memeDisplayTimedDuration: Int,
  var memeVolume: Int,
  var soundEnabled: Boolean,
  var preferredGenders: Int,
  var allowFrustration: Boolean,
  var probabilityOfFrustration: Int,
  var enabledEvents: Int,
  var logSearchTerms: String,
  var logSearchIgnoreCase: Boolean,
  var showMood: Boolean,
  var eventsBeforeFrustration: Int,
) {
  fun duplicate(): ConfigSettingsModel = copy()
}

object PluginSettings {
  const val PLUGIN_SETTINGS_DISPLAY_NAME = "AMII Settings"
  val CHANGELOG_URI =
    URI("https://github.com/Unthrottled/AMII/blob/master/CHANGELOG.md")
  private const val REPOSITORY = "https://github.com/Unthrottled/AMII"
  val ISSUES_URI = URI("$REPOSITORY/issues")

  @JvmStatic
  fun getInitialConfigSettingsModel() = ConfigSettingsModel(
    allowedExitCodes = Config.instance.allowedExitCodes,
    idleTimeoutInMinutes = Config.instance.idleTimeoutInMinutes,
    silenceTimeoutInMinutes = Config.instance.silenceTimeoutInMinutes,
    memeDisplayAnchorValue = Config.instance.memeDisplayAnchorValue,
    memeDisplayModeValue = Config.instance.memeDisplayModeValue,
    idleMemeDisplayAnchorValue = Config.instance.idleMemeDisplayAnchorValue,
    memeDisplayInvulnerabilityDuration = Config.instance.memeDisplayInvulnerabilityDuration,
    memeDisplayTimedDuration = Config.instance.memeDisplayTimedDuration,
    memeVolume = Config.instance.memeVolume,
    soundEnabled = Config.instance.soundEnabled,
    preferredGenders = Config.instance.preferredGenders,
    allowFrustration = Config.instance.allowFrustration,
    probabilityOfFrustration = Config.instance.probabilityOfFrustration,
    enabledEvents = Config.instance.enabledEvents,
    logSearchTerms = Config.instance.logSearchTerms,
    logSearchIgnoreCase = Config.instance.logSearchIgnoreCase,
    showMood = Config.instance.showMood,
    eventsBeforeFrustration = Config.instance.eventsBeforeFrustration,
  )
}
