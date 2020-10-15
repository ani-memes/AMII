package io.unthrottled.amii.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import io.unthrottled.amii.AMII.PLUGIN_ID

class PluginInstallListener : DynamicPluginListener {

  override fun beforePluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun beforePluginUnload(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }

  override fun checkUnloadPlugin(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (pluginDescriptor.pluginId.idString == PLUGIN_ID) {
      // todo: this
    }
  }

  override fun pluginUnloaded(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {}
}
