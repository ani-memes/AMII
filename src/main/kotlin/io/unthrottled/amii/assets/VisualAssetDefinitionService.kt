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
  ) =
    chooseAssetAtRandom(
      VisualEntityService.instance.supplyPreferredLocalAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .ifEmpty {
          VisualEntityService.instance.supplyPreferredGenderLocalAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }.ifEmpty {
          VisualEntityService.instance.supplyAllLocalAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    )
      .map {
        resolveAsset(memeAssetCategory, it.representation)
      }.orElseGet {
        fetchRemoteAsset(memeAssetCategory)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    assetDefinition: VisualAssetRepresentation,
  ): Optional<VisualMemeContent> {
    downloadNewAsset(memeAssetCategory)
    return remoteAssetManager.resolveAsset(assetDefinition)
  }

  private fun downloadNewAsset(
    memeAssetCategory: MemeAssetCategory,
  ) {
    ExecutionService.executeAsynchronously {
      fetchRemoteAsset(memeAssetCategory)
    }
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory,
  ): Optional<VisualMemeContent> =
    chooseAssetAtRandom(
      VisualEntityService.instance.supplyPreferredRemoteAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .ifEmpty {
          VisualEntityService.instance.supplyPreferredGenderRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }.ifEmpty {
          VisualEntityService.instance.supplyAllRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    ).flatMap { remoteAssetManager.resolveAsset(it.representation) }

  private fun chooseAssetAtRandom(
    assetDefinitions: Collection<VisualAssetEntity>
  ): Optional<VisualAssetEntity> =
    assetDefinitions
      .toOptional()
      .filter { it.isNotEmpty() }
      .flatMap { VisualAssetProbabilityService.instance.pickAssetFromList(it) }
}

fun Collection<VisualAssetEntity>.filterByCategory(
  category: MemeAssetCategory
): Collection<VisualAssetEntity> =
  this.filter { it.assetCategories.contains(category) }
