package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object AudibleAssetDefinitionService {

  fun getAssetById(assetId: String): Optional<AudibleMemeAsset> =
    AudibleAssetManager.supplyAllAssetDefinitions()
      .find { it.id == assetId }
      .toOptional()
      .flatMap { AudibleAssetManager.resolveAsset(it) }
}
