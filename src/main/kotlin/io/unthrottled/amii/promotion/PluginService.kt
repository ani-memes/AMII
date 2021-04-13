package io.unthrottled.amii.promotion

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import io.unthrottled.amii.config.Constants
import io.unthrottled.amii.tools.toOptional

object PluginService {

  fun isRiderExtensionInstalled(): Boolean = PluginManagerCore.isPluginInstalled(
    PluginId.getId(Constants.RIDER_EXTENSION_ID)
  )

  fun canRiderExtensionBeInstalled(): Boolean =
    PluginManagerCore.getPlugin(PluginId.getId(Constants.RIDER_EXTENSION_ID)).toOptional()
      .filter { PluginManagerCore.isCompatible(it) }
      .isPresent
}
