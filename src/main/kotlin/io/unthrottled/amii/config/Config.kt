package io.unthrottled.amii.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.listeners.FORCE_KILLED_EXIT_CODE
import io.unthrottled.amii.listeners.OK_EXIT_CODE
import io.unthrottled.amii.memes.PanelDismissalOptions

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
    const val DEFAULT_MEME_INVULNERABLE_DURATION: Int = 3
    const val DEFAULT_TIMED_MEME_DISPLAY_DURATION: Int = 40
  }

  var memeDisplayModeValue: String = PanelDismissalOptions.TIMED.toString()
  var memeDisplayAnchorValue: String = NotificationAnchor.TOP_RIGHT.toString()
  var memeDisplayInvulnerabilityDuration: Int = DEFAULT_MEME_INVULNERABLE_DURATION
  var memeDisplayTimedDuration: Int = DEFAULT_TIMED_MEME_DISPLAY_DURATION
  var userId: String = ""
  var version: String = ""
  var allowedExitCodes = listOf(
    OK_EXIT_CODE,
    FORCE_KILLED_EXIT_CODE
  ).joinToString(DEFAULT_DELIMITER)
  var idleTimeoutInMinutes = DEFAULT_IDLE_TIMEOUT_IN_MINUTES

  override fun getState(): Config? =
    createCopy(this)

  override fun loadState(state: Config) {
    copyBean(state, this)
  }

  val notificationAnchor: NotificationAnchor
    get() = NotificationAnchor.fromValue(memeDisplayAnchorValue)

  val notificationMode: PanelDismissalOptions
    get() = PanelDismissalOptions.fromValue(memeDisplayModeValue)
}
