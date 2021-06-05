package io.unthrottled.amii.assets

object VisualEntitySupplier {

  fun getLocalAssetsByCategory(memeAssetCategory: MemeAssetCategory) =
    VisualEntityService.instance.supplyPreferredLocalAssetDefinitions()
      .filterByCategory(memeAssetCategory)
      .ifEmpty {
        VisualEntityService.instance.supplyPreferredGenderLocalAssetDefinitions()
          .filterByCategory(memeAssetCategory)
      }.ifEmpty {
        VisualEntityService.instance.supplyAllLocalAssetDefinitions()
          .filterByCategory(memeAssetCategory)
      }

  fun getRemoteAssetsByCategory(memeAssetCategory: MemeAssetCategory) =
    getPreferredRemoteAssets(memeAssetCategory)
      .ifEmpty {
        VisualEntityService.instance.supplyAllRemoteAssetDefinitions()
          .filterByCategory(memeAssetCategory)
      }

  fun getPreferredRemoteAssets(memeAssetCategory: MemeAssetCategory) =
    VisualEntityService.instance.supplyPreferredRemoteAssetDefinitions()
      .filterByCategory(memeAssetCategory)
      .ifEmpty {
        VisualEntityService.instance.supplyPreferredGenderRemoteAssetDefinitions()
          .filterByCategory(memeAssetCategory)
      }
}
