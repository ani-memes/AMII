package io.unthrottled.amii.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy
import io.unthrottled.amii.listeners.FORCE_KILLED_EXIT_CODE
import io.unthrottled.amii.listeners.OK_EXIT_CODE

@State(
  name = "Plugin-Config",
  storages = [Storage("AMII.xml")]
)
class Config : PersistentStateComponent<Config>, Cloneable {
  companion object {
    val instance: Config
      get() = ServiceManager.getService(Config::class.java)
    const val DEFAULT_DELIMITER = ","
    const val DEFAULT_IDLE_TIMEOUT_IN_MINUTES: Long = 5L
  }

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
}
