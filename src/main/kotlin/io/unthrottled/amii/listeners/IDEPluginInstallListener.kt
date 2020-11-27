package io.unthrottled.amii.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.amii.config.Constants.PLUGIN_ID

fun interface PluginUpdateListener {
  fun onUpdate()
}

val PLUGIN_UPDATE_TOPIC = Topic.create(
  "AMII Update",
  PluginUpdateListener::class.java
)

class IDEPluginInstallListener : DynamicPluginListener {

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
      ApplicationManager.getApplication()
        .messageBus.syncPublisher(PLUGIN_UPDATE_TOPIC)
        .onUpdate()
    }
  }

  override fun pluginUnloaded(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }
}
