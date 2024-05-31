package io.unthrottled.amii.promotion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.extensions.PluginId
import com.intellij.util.Urls
import com.intellij.util.io.HttpRequests
import io.unthrottled.amii.config.Constants
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.util.Collections
import java.util.concurrent.Callable

object PluginService : Logging {

  private val objectMapper by lazy { ObjectMapper() }

  private val PLUGIN_MANAGER_URL by lazy {
    ApplicationInfo.getInstance()
      .toOptional()
      .filter { it is ApplicationInfoEx }
      .map { it as ApplicationInfoEx }
      .map { it.pluginManagerUrl }
      .orElseGet { "https://plugins.jetbrains.com" }
      .trimEnd('/')
  }
  private val COMPATIBLE_UPDATE_URL by lazy { "$PLUGIN_MANAGER_URL/api/search/compatibleUpdates" }

  fun isRiderExtensionInstalled(): Boolean = PluginManagerCore.isPluginInstalled(
    PluginId.getId(Constants.RIDER_EXTENSION_ID)
  )

  fun isAndroidExtensionInstalled(): Boolean = PluginManagerCore.isPluginInstalled(
    PluginId.getId(Constants.ANDROID_EXTENSION_ID)
  )

  fun canRiderExtensionBeInstalled(): Boolean =
    canExtensionBeInstalled(Constants.RIDER_EXTENSION_ID)

  fun canAndroidExtensionBeInstalled(): Boolean =
    canExtensionBeInstalled(Constants.ANDROID_EXTENSION_ID)

  private fun canExtensionBeInstalled(pluginIdString: String) =
    ApplicationManager.getApplication().executeOnPooledThread(
      Callable {
        val pluginId = PluginId.getId(pluginIdString)
        runSafelyWithResult({
          getLastCompatiblePluginUpdate(
            Collections.singleton(pluginId)
          ).firstOrNull { pluginNode ->
            pluginNode.pluginId == pluginIdString
          } != null
        }) {
          false
        }
      }
    ).get()

  private data class CompatibleUpdateRequest(
    val build: String,
    val pluginXMLIds: List<String>
  )

  /**
   * Object from Search Service for getting compatible updates for IDE.
   * [externalUpdateId] update ID from Plugin Repository database.
   * [externalPluginId] plugin ID from Plugin Repository database.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  data class IdeCompatibleUpdate(
    @get:JsonProperty("id")
    val externalUpdateId: String = "",
    @get:JsonProperty("pluginId")
    val externalPluginId: String = "",
    @get:JsonProperty("pluginXmlId")
    val pluginId: String = "",
    val version: String = ""
  )

  private fun getLastCompatiblePluginUpdate(
    ids: Set<PluginId>
  ): List<IdeCompatibleUpdate> {
    return try {
      if (ids.isEmpty()) {
        return emptyList()
      }

      val data = objectMapper.writeValueAsString(
        CompatibleUpdateRequest(
          ApplicationInfo.getInstance()
            .build.asString(),
          ids.map { it.idString }
        )
      )
      HttpRequests
        .post(Urls.newFromEncoded(COMPATIBLE_UPDATE_URL).toExternalForm(), HttpRequests.JSON_CONTENT_TYPE)
        .productNameAsUserAgent()
        .throwStatusCodeException(false)
        .connect {
          it.write(data)
          objectMapper.readValue(it.inputStream, object : TypeReference<List<IdeCompatibleUpdate>>() {})
        }
    } catch (e: Exception) {
      logger().warn("Unable to check to see if plugin $ids is compatible for reasons", e)
      emptyList()
    }
  }
}
