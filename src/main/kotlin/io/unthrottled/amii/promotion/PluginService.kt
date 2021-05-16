package io.unthrottled.amii.promotion

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.marketplace.MarketplaceRequests
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import io.unthrottled.amii.config.Constants
import io.unthrottled.amii.tools.runSafelyWithResult
import java.util.Collections
import java.util.concurrent.Callable

object PluginService {

  fun isRiderExtensionInstalled(): Boolean = PluginManagerCore.isPluginInstalled(
    PluginId.getId(Constants.RIDER_EXTENSION_ID)
  )

  fun canRiderExtensionBeInstalled(): Boolean =
    canExtensionBeInstalled(Constants.RIDER_EXTENSION_ID)

  fun canAndroidExtensionBeInstalled(): Boolean =
    canExtensionBeInstalled(Constants.ANDROID_EXTENSION_ID)

  private fun canExtensionBeInstalled(riderExtensionId: String) =
    ApplicationManager.getApplication().executeOnPooledThread(
      Callable {
        val pluginId = PluginId.getId(riderExtensionId)
        runSafelyWithResult({
          MarketplaceRequests.getInstance().loadLastCompatiblePluginDescriptors(
            Collections.singletonList(riderExtensionId)
          ).firstOrNull { pluginNode ->
            pluginNode.pluginId == pluginId
          } != null
        }) {
          false
        }
      }
    ).get()
}
