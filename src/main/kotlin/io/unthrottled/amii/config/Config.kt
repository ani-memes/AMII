package io.unthrottled.amii.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy
import io.unthrottled.amii.assets.Gender
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.listeners.FORCE_KILLED_EXIT_CODE
import io.unthrottled.amii.listeners.OK_EXIT_CODE
import io.unthrottled.amii.memes.PanelDismissalOptions
import io.unthrottled.amii.tools.lt

@State(
  name = "Plugin-Config",
  storages = [Storage("AMII.xml")]
)
class Config : PersistentStateComponent<Config>, Cloneable {
  companion object {
    @JvmStatic
    val instance: Config
      get() = ServiceManager.getService(Config::class.java)
    const val DEFAULT_DELIMITER = ","
    const val DEFAULT_IDLE_TIMEOUT_IN_MINUTES: Long = 5L
    const val DEFAULT_SILENCE_TIMEOUT_IN_MINUTES: Long = 20L
    const val DEFAULT_MEME_INVULNERABLE_DURATION: Int = 3
    const val DEFAULT_TIMED_MEME_DISPLAY_DURATION: Int = 40
    const val DEFAULT_EVENTS_BEFORE_FRUSTRATION: Int = 5
    const val DEFAULT_FRUSTRATION_PROBABILITY: Int = 75
    const val DEFAULT_VOLUME_LEVEL: Int = 65
    private const val MAX_VOLUME = 100
    private val ignoredEvents = setOf(
      UserEvents.LOGS,
      UserEvents.SILENCE
    )
  }

  var memeVolume: Int = DEFAULT_VOLUME_LEVEL
  var soundEnabled = true
  var memeDisplayModeValue: String = PanelDismissalOptions.TIMED.toString()
  var memeDisplayAnchorValue: String = NotificationAnchor.TOP_RIGHT.toString()
  var idleMemeDisplayAnchorValue: String = NotificationAnchor.CENTER.toString()
  var memeDisplayInvulnerabilityDuration: Int = DEFAULT_MEME_INVULNERABLE_DURATION
  var memeDisplayTimedDuration: Int = DEFAULT_TIMED_MEME_DISPLAY_DURATION
  var userId: String = ""
  var version: String = ""
  var allowedExitCodes = listOf(
    OK_EXIT_CODE,
    FORCE_KILLED_EXIT_CODE
  ).joinToString(DEFAULT_DELIMITER)
  var positiveExitCodes = ""
  var idleTimeoutInMinutes = DEFAULT_IDLE_TIMEOUT_IN_MINUTES
  var silenceTimeoutInMinutes = DEFAULT_SILENCE_TIMEOUT_IN_MINUTES
  var allowFrustration = true
  var eventsBeforeFrustration = DEFAULT_EVENTS_BEFORE_FRUSTRATION
  var probabilityOfFrustration = DEFAULT_FRUSTRATION_PROBABILITY
  var preferredCharacters = ""
  var blackListedCharacters = ""
  var logSearchTerms = ""
  var logSearchIgnoreCase = true
  var preferredGenders: Int = allGenders()
  var enabledEvents: Int = allEvents()
  var showMood: Boolean = true
  var minimalMode: Boolean = false
  var capDimensions: Boolean = false
  var maxMemeWidth: Int = -1
  var maxMemeHeight: Int = -1

  private fun allGenders() = Gender.values().map { it.value }.reduce { acc, i -> acc or i }
  private fun allEvents() = UserEvents.values()
    .filter { ignoredEvents.contains(it).not() }
    .map { it.value }.reduce { acc, i -> acc or i }

  override fun getState(): Config? =
    createCopy(this)

  override fun loadState(state: Config) {
    copyBean(state, this)
  }

  val notificationAnchor: NotificationAnchor
    get() = NotificationAnchor.fromValue(memeDisplayAnchorValue)

  val idleNotificationAnchor: NotificationAnchor
    get() = NotificationAnchor.fromValue(idleMemeDisplayAnchorValue)

  val volume: Float
    get() =
      when (memeVolume) {
        in lt(0) -> 0F
        in 0..MAX_VOLUME -> memeVolume / MAX_VOLUME.toFloat()
        else -> 1F
      }

  fun eventEnabled(userEvent: UserEvents): Boolean =
    (enabledEvents and userEvent.value) == userEvent.value

  fun genderPreferred(gender: Gender): Boolean =
    preferredGenders == 0 ||
      (preferredGenders and gender.value) == gender.value

  val notificationMode: PanelDismissalOptions
    get() = PanelDismissalOptions.fromValue(memeDisplayModeValue)
}
