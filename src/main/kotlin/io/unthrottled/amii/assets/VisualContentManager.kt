package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.net.URI
import java.util.Optional

object VisualContentManager :
  RemoteContentManager<VisualMemeAssetDefinition, VisualMemeContent>(
    AssetCategory.VISUALS
  ),
  Logging {

  // todo: these
  override fun supplyPreferredLocalAssetDefinitions(): Set<VisualMemeAssetDefinition> =
    super.supplyPreferredLocalAssetDefinitions()

  override fun supplyPreferredRemoteAssetDefinitions(): List<VisualMemeAssetDefinition> =
    super.supplyPreferredRemoteAssetDefinitions()

  override fun convertToAsset(
    asset: VisualMemeAssetDefinition,
    assetUrl: URI
  ): VisualMemeContent =
    asset.toContent(assetUrl)

  override fun convertToDefinitions(defJson: String): Optional<List<VisualMemeAssetDefinition>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualMemeAssetDefinition>>(
        defJson,
        object : TypeToken<List<VisualMemeAssetDefinition>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }
}
