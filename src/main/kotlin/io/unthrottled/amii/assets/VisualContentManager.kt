package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.Optional

object VisualContentManager {

  fun supplyAllLocalAssetDefinitions(): Set<VisualAssetRepresentation> {
    return if (Config.instance.onlyCustomAssets) {
      LocalVisualContentManager.supplyAllExistingVisualAssetRepresentations()
    } else {
      RemoteVisualContentManager.supplyAllLocalAssetDefinitions() +
        LocalVisualContentManager.supplyAllExistingVisualAssetRepresentations()
    }
  }
}

object RemoteVisualContentManager :
  RemoteContentManager<VisualAssetRepresentation, VisualMemeContent>(
    AssetCategory.VISUALS
  ),
  Logging {

  override fun convertToAsset(
    asset: VisualAssetRepresentation,
    assetUrl: URI
  ): VisualMemeContent =
    VisualMemeContent(asset.id, assetUrl, asset.alt, asset.aud)

  override fun convertToDefinitions(defJson: String): Optional<List<VisualAssetRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualAssetRepresentation>>(
        defJson,
        object : TypeToken<List<VisualAssetRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }

  override fun convertToDefinitions(defJson: InputStream): Optional<List<VisualAssetRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualAssetRepresentation>>(
        JsonReader(InputStreamReader(defJson)),
        object : TypeToken<List<VisualAssetRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }
}
