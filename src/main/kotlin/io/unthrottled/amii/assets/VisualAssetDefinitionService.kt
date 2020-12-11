package io.unthrottled.amii.assets

import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import kotlin.random.Random

object VisualAssetDefinitionService : Logging {

  private val remoteAssetManager = VisualContentManager

  private val random = Random(System.currentTimeMillis())

  fun getRandomAssetByCategory(
    memeAssetCategory: MemeAssetCategory,
  ): Optional<VisualMemeContent> =
    chooseRandomAsset(memeAssetCategory)

  private fun chooseRandomAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinition) -> Boolean = { true }
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
    assetDefinition: VisualMemeAssetDefinition,
    assetPredicate: (VisualMemeAssetDefinition) -> Boolean = { true },
  ): Optional<VisualMemeContent> {
    downloadNewAsset(memeAssetCategory, assetPredicate)
    return remoteAssetManager.resolveAsset(assetDefinition)
  }

  private fun downloadNewAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinition) -> Boolean
  ) {
    ExecutionService.executeAsynchronously {
      fetchRemoteAsset(memeAssetCategory, assetPredicate)
    }
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (VisualMemeAssetDefinition) -> Boolean,
  ): Optional<VisualMemeContent> =
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
    assetDefinitions: Collection<VisualMemeAssetDefinition>
  ): Optional<VisualMemeAssetDefinition> =
    assetDefinitions
      .toOptional()
      .filter { it.isNotEmpty() }
      .map { it.random(random) }
}

fun Collection<VisualMemeAssetDefinition>.filterByCategory(
  category: MemeAssetCategory
): Collection<VisualMemeAssetDefinition> =
  this.filter { it.cat.contains(category.ordinal) } // todo: revisit
