package io.unthrottled.amii.assets

import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import kotlin.random.Random

object VisualAssetDefinitionServiceV2 : Logging {

  private val remoteAssetManager = VisualAssetManagerV2

  private val random = Random(System.currentTimeMillis())

  fun getRandomAssetByCategory(
    memeAssetCategory: MemeAssetCategory,
  ): Optional<VisualMemeAssetV2> =
    chooseRandomAsset(memeAssetCategory)

  private fun chooseRandomAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinitionV2) -> Boolean = { true }
  ) =
    chooseAssetAtRandom(
      remoteAssetManager.supplyPreferredLocalAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .filter(assetPredicate)
        .ifEmpty {
          remoteAssetManager.supplyAllLocalAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    )
      .map {
        resolveAsset(memeAssetCategory, it, assetPredicate)
      }.orElseGet {
        fetchRemoteAsset(memeAssetCategory, assetPredicate)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    assetDefinition: VisualMemeAssetDefinitionV2,
    assetPredicate: (VisualMemeAssetDefinitionV2) -> Boolean = { true },
  ): Optional<VisualMemeAssetV2> {
    downloadNewAsset(memeAssetCategory, assetPredicate)
    return remoteAssetManager.resolveAsset(assetDefinition)
  }

  private fun downloadNewAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinitionV2) -> Boolean
  ) {
    ExecutionService.executeAsynchronously {
      fetchRemoteAsset(memeAssetCategory, assetPredicate)
    }
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinitionV2) -> Boolean,
  ): Optional<VisualMemeAssetV2> =
    chooseAssetAtRandom(
      remoteAssetManager.supplyPreferredRemoteAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .filter(assetPredicate)
        .ifEmpty {
          remoteAssetManager.supplyAllRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    ).flatMap { remoteAssetManager.resolveAsset(it) }

  private fun chooseAssetAtRandom(
    assetDefinitions: Collection<VisualMemeAssetDefinitionV2>
  ): Optional<VisualMemeAssetDefinitionV2> =
    assetDefinitions
      .toOptional()
      .filter { it.isNotEmpty() }
      .map { it.random(random) }
}

fun Collection<VisualMemeAssetDefinitionV2>.filterByCategory(
  category: MemeAssetCategory
): Collection<VisualMemeAssetDefinitionV2> =
  this.filter { it.cat.contains(category.ordinal) } // todo: revisit
