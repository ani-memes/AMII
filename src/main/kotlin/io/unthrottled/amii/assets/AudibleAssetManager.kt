package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.net.URI
import java.util.Optional

object AudibleAssetManager :
  RemoteAssetManagerV2<AudibleAssetDefinition, AudibleMemeAsset>(
    AssetCategory.AUDIBLE
  ),
  Logging {

  override fun convertToAsset(
    asset: AudibleAssetDefinition,
    assetUrl: URI
  ): AudibleMemeAsset =
    asset.toAsset(assetUrl)

  override fun convertToDefinitions(defJson: String): Optional<List<AudibleAssetDefinition>> =
    runSafelyWithResult({
      Gson().fromJson<List<AudibleAssetDefinition>>(
        defJson,
        object : TypeToken<List<AudibleAssetDefinition>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read audible Assets for reasons $defJson", it)
      Optional.empty()
    }
}
