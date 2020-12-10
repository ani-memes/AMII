package io.unthrottled.amii.assets

import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import java.util.UUID
import kotlin.random.Random

@SuppressWarnings("UnnecessaryAbstractClass")
abstract class RemoteAssetDefinitionServiceV2<T : AssetDefinition, U : Asset>(
  private val remoteAssetManager: RemoteAssetManager<T, U>
) {
  private val random = Random(System.currentTimeMillis())

  fun getAssetByGroupId(
    groupId: UUID,
    category: MemeAssetCategory,
  ): Optional<U> =
    remoteAssetManager.supplyPreferredLocalAssetDefinitions()
      .find {
        it.groupId == groupId
      }.toOptional()
      .map {
        resolveAsset(category, it)
      }.orElseGet {
        remoteAssetManager.supplyPreferredRemoteAssetDefinitions()
          .find { it.groupId == groupId }
          .toOptional()
          .map { remoteAssetManager.resolveAsset(it) }
          .orElseGet {
            getRandomAssetByCategory(category)
          }
      }

  fun getRandomAssetByCategory(
    memeAssetCategory: MemeAssetCategory,
  ): Optional<U> =
    chooseRandomAsset(memeAssetCategory)

  fun getRandomUngroupedAssetByCategory(
    memeAssetCategory: MemeAssetCategory,
  ): Optional<U> =
    chooseRandomAsset(memeAssetCategory) { it.groupId == null }

  private fun chooseRandomAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (T) -> Boolean = { true }
  ) =
    chooseRandomAsset(
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
    assetDefinition: T,
    assetPredicate: (T) -> Boolean = { true },
  ): Optional<U> {
    downloadNewAsset(memeAssetCategory, assetPredicate)
    return remoteAssetManager.resolveAsset(assetDefinition)
  }

  private fun downloadNewAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (T) -> Boolean
  ) {
    ExecutionService.executeAsynchronously {
      fetchRemoteAsset(memeAssetCategory, assetPredicate)
    }
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (T) -> Boolean,
  ): Optional<U> =
    chooseRandomAsset(
      remoteAssetManager.supplyPreferredRemoteAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .filter(assetPredicate)
        .ifEmpty {
          remoteAssetManager.supplyAllRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    ).flatMap { remoteAssetManager.resolveAsset(it) }

  private fun chooseRandomAsset(
    assetDefinitions: Collection<T>
  ): Optional<T> =
    assetDefinitions
      .toOptional()
      .filter { it.isNotEmpty() }
      .map { it.random(random) }
}
