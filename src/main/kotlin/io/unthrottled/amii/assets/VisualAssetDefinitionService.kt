package io.unthrottled.amii.assets

import io.unthrottled.amii.assets.VisualEntitySupplier.getLocalAssetsByCategory
import io.unthrottled.amii.assets.VisualEntitySupplier.getRemoteAssetsByCategory
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.toOptional
import java.net.URI
import java.util.Optional

object VisualAssetDefinitionService : Logging {

  private val assetManager = RemoteVisualContentManager

  fun getRandomAssetByCategory(
    memeAssetCategory: MemeAssetCategory
  ): Optional<VisualMemeContent> =
    chooseAssetAtRandom(getLocalAssetsByCategory(memeAssetCategory))
      .map {
        resolveAsset(memeAssetCategory, it)
      }.orElseGet {
        fetchRemoteAsset(memeAssetCategory)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    entity: VisualAssetEntity
  ): Optional<VisualMemeContent> {
    BackgroundAssetService.downloadNewAssets(memeAssetCategory)
    return if (entity.isCustomAsset) {
      assetManager.convertToAsset(entity.representation, URI(entity.path))
        .toOptional()
    } else {
      assetManager.resolveAsset(entity.representation)
    }
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory
  ): Optional<VisualMemeContent> {
    BackgroundAssetService.downloadNewAssets(memeAssetCategory)
    return chooseAssetAtRandom(
      getRemoteAssetsByCategory(memeAssetCategory)
    ).flatMap { assetManager.resolveAsset(it.representation) }
  }
}

fun chooseAssetAtRandom(
  assetDefinitions: Collection<VisualAssetEntity>
): Optional<VisualAssetEntity> =
  assetDefinitions
    .toOptional()
    .filter { it.isNotEmpty() }
    .flatMap { VisualAssetProbabilityService.instance.pickAssetFromList(it) }

fun Collection<VisualAssetEntity>.filterByCategory(
  category: MemeAssetCategory
): Collection<VisualAssetEntity> =
  this.filter { it.assetCategories.contains(category) }
