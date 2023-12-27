package io.unthrottled.amii.assets

data class VisualAssetStorage(
  val savedVisualAssets: Map<String, VisualAssetRepresentation>
)

object LocalVisualAssetStorageService : LocalPersistenceService<VisualAssetStorage>(
  "user-visual-representations.json",
  VisualAssetStorage::class.java
) {
  override fun decorateItem(item: VisualAssetStorage): VisualAssetStorage = item

  override fun buildDefaultLedger() =
    VisualAssetStorage(HashMap())

  override fun combineWithOnDisk(themeObservationLedger: VisualAssetStorage): VisualAssetStorage {
    return themeObservationLedger
  }
}
