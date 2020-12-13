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
      remoteAssetManager.supplyPreferredLocalAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .ifEmpty {
          remoteAssetManager.supplyPreferredGenderLocalAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }.ifEmpty {
          remoteAssetManager.supplyAllLocalAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    )
      .map {
        resolveAsset(memeAssetCategory, it)
      }.orElseGet {
        fetchRemoteAsset(memeAssetCategory)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    assetDefinition: VisualMemeAssetDefinition,
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
      remoteAssetManager.supplyPreferredRemoteAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .ifEmpty {
          remoteAssetManager.supplyPreferredGenderRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }.ifEmpty {
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
