package io.unthrottled.amii.promotion

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.marketplace.MarketplaceRequests
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import io.unthrottled.amii.config.Constants
import java.util.Collections
import java.util.concurrent.Callable

object PluginService {

  fun isRiderExtensionInstalled(): Boolean = PluginManagerCore.isPluginInstalled(
    PluginId.getId(Constants.RIDER_EXTENSION_ID)
  )

  fun canRiderExtensionBeInstalled(): Boolean {
    return ApplicationManager.getApplication().executeOnPooledThread(
      Callable {
        val ids = Constants.RIDER_EXTENSION_ID
        val pluginId = PluginId.getId(ids)
        MarketplaceRequests.getInstance().loadLastCompatiblePluginDescriptors(
          Collections.singletonList(ids)
        ).firstOrNull { pluginNode ->
          pluginNode.pluginId == pluginId
        } != null
      }
    ).get()
  }
}
