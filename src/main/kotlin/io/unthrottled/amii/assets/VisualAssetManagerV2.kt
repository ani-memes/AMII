package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.net.URI
import java.util.Optional

object VisualAssetManagerV2 :
  RemoteAssetManagerV2<VisualMemeAssetDefinitionV2, VisualMemeAssetV2>(
    AssetCategory.VISUALS
  ),
  Logging {

  // todo: these
  override fun supplyPreferredLocalAssetDefinitions(): Set<VisualMemeAssetDefinitionV2> =
    super.supplyPreferredLocalAssetDefinitions()

  override fun supplyPreferredRemoteAssetDefinitions(): List<VisualMemeAssetDefinitionV2> =
    super.supplyPreferredRemoteAssetDefinitions()

  override fun convertToAsset(
    asset: VisualMemeAssetDefinitionV2,
    assetUrl: URI
  ): VisualMemeAssetV2 =
    asset.toAsset(assetUrl)

  override fun convertToDefinitions(defJson: String): Optional<List<VisualMemeAssetDefinitionV2>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualMemeAssetDefinitionV2>>(
        defJson,
        object : TypeToken<List<VisualMemeAssetDefinitionV2>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }
}
