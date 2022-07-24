package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.nio.file.Paths
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

  private var ledger = LocalAudibleAssetStorageService.getInitialItem()

  fun save(audibleContent: AudibleRepresentation) {
    val newMap = ledger.savedAudibleAssets.toMutableMap()
    newMap[audibleContent.id] = audibleContent
    ledger = ledger.copy(savedAudibleAssets = newMap)
    LocalAudibleAssetStorageService.persistLedger(ledger)
  }

  fun getAssetById(assetId: String): Optional<AudibleContent> =
    ledger.savedAudibleAssets[assetId].toOptional()
      .map {
        AudibleContent(Paths.get(it.path).toUri())
      }
}
