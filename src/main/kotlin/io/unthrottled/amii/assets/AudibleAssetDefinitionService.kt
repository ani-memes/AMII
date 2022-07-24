package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object AudibleAssetDefinitionService {

  fun getAssetById(assetId: String): Optional<AudibleContent> {
    val localDef = AudibleContentManager.supplyAllAssetDefinitions()
      .find { it.id == assetId }
    return if(localDef != null) {
      localDef
        .toOptional()
        .flatMap { AudibleContentManager.resolveAsset(it) }
    } else {
      LocalAudibleDefinitionService.getAssetById(assetId)
    }
  }
}

object LocalAudibleDefinitionService {

  fun save(audibleContent: AudibleRepresentation) {
    // todo: this
  }

  fun getAssetById(assetId: String): Optional<AudibleContent> =
    Optional.empty() // todo: this:


}
