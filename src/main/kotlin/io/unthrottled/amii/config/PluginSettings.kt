package io.unthrottled.amii.config

import java.net.URI

data class ConfigSettingsModel(
  var allowedExitCodes: String,
  var positiveExitCodes: String,
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
  var minimalMode: Boolean,
  var discreetMode: Boolean,
  var capDimensions: Boolean,
  var infoOnClick: Boolean,
  var allowLewds: Boolean,
  var createAutoTagDirectories: Boolean,
  var maxMemeWidth: Int,
  var maxMemeHeight: Int,
  var customAssetsPath: String,
) {

  fun duplicate(): ConfigSettingsModel = copy()
}

object PluginSettings {
  const val PLUGIN_SETTINGS_DISPLAY_NAME = "AMII Settings"
  val CHANGELOG_URI =
    URI("https://github.com/ani-memes/AMII/blob/master/CHANGELOG.md")
  private const val REPOSITORY = "https://github.com/ani-memes/AMII"
  val ISSUES_URI = URI("$REPOSITORY/issues")

  @JvmStatic
  fun getInitialConfigSettingsModel() = ConfigSettingsModel(
    allowedExitCodes = Config.instance.allowedExitCodes,
    positiveExitCodes = Config.instance.positiveExitCodes,
    idleTimeoutInMinutes = Config.instance.idleTimeoutInMinutes,
    silenceTimeoutInMinutes = Config.instance.silenceTimeoutInMinutes,
    memeDisplayAnchorValue = Config.instance.memeDisplayAnchorValue,
    idleMemeDisplayAnchorValue = Config.instance.idleMemeDisplayAnchorValue,
    memeDisplayModeValue = Config.instance.memeDisplayModeValue,
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
    minimalMode = Config.instance.minimalMode,
    discreetMode = Config.instance.discreetMode,
    capDimensions = Config.instance.capDimensions,
    infoOnClick = Config.instance.infoOnClick,
    allowLewds = Config.instance.allowLewds,
    createAutoTagDirectories = Config.instance.createAutoTagDirectories,
    maxMemeWidth = Config.instance.maxMemeWidth,
    maxMemeHeight = Config.instance.maxMemeHeight,
    customAssetsPath = Config.instance.customAssetsPath,
  )
}
