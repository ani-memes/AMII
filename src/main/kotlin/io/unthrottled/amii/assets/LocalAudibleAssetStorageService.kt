package io.unthrottled.amii.assets

data class AudibleAssetStorage(
  val savedAudibleAssets: Map<String, AudibleRepresentation>
)

object LocalAudibleAssetStorageService : LocalPersistenceService<AudibleAssetStorage>(
  "user-audible-representations.json",
  AudibleAssetStorage::class.java
) {
  override fun decorateItem(item: AudibleAssetStorage): AudibleAssetStorage = item

  override fun buildDefaultLedger() =
    AudibleAssetStorage(HashMap())

  override fun combineWithOnDisk(themeObservationLedger: AudibleAssetStorage): AudibleAssetStorage {
    return themeObservationLedger
  }
}
