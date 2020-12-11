package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object AudibleAssetDefinitionService {

  fun getAssetById(assetId: String): Optional<AudibleMemeContent> =
    AudibleContentManager.supplyAllAssetDefinitions()
      .find { it.id == assetId }
      .toOptional()
      .flatMap { AudibleContentManager.resolveAsset(it) }
}
