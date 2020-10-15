package io.unthrottled.amii.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.XmlSerializerUtil.createCopy

@State(
  name = "Plugin-Config",
  storages = [Storage("AMII.xml")]
)
class PluginConfig : PersistentStateComponent<PluginConfig>, Cloneable {
  companion object {
    val instance: PluginConfig
      get() = ServiceManager.getService(PluginConfig::class.java)
  }

  var userId: String = ""
  var version: String = ""

  override fun getState(): PluginConfig? =
    createCopy(this)

  override fun loadState(state: PluginConfig) {
    copyBean(state, this)
  }
}
