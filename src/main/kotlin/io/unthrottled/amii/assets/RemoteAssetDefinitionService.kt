package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import java.util.UUID
import kotlin.random.Random

@SuppressWarnings("UnnecessaryAbstractClass")
abstract class RemoteAssetDefinitionService<T : AssetDefinition, U : Asset>(
  private val remoteAssetManager: RemoteAssetManager<T, U>
) {
  private val random = Random(System.currentTimeMillis())

  fun getAssetByGroupId(
    groupId: UUID,
    category: MemeAssetCategory,
  ): Optional<U> =
    remoteAssetManager.supplyLocalAssetDefinitions()
      .find {
        it.groupId == groupId
      }.toOptional()
      .map {
        resolveAsset(category, it)
      }.orElseGet {
        remoteAssetManager.supplyRemoteAssetDefinitions()
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
    waifuAssetCategory: MemeAssetCategory,
  ): Optional<U> =
    chooseRandomAsset(waifuAssetCategory) { it.groupId == null }

  private fun chooseRandomAsset(
    waifuAssetCategory: MemeAssetCategory,
    assetPredicate: (T) -> Boolean = { true }
  ) =
    chooseRandomAsset(
      // todo: handle folks with the slow internet
      remoteAssetManager.supplyAllAssetDefinitions()
        .filterByCategory(waifuAssetCategory)
        .filter(assetPredicate)
        .ifEmpty {
          remoteAssetManager.supplyAllLocalAssetDefinitions()
            .filterByCategory(waifuAssetCategory)
        }
    )
      .map {
        resolveAsset(waifuAssetCategory, it)
      }.orElseGet {
        fetchRemoteAsset(waifuAssetCategory, assetPredicate)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    it: T
  ): Optional<U> {
    return remoteAssetManager.resolveAsset(it)
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory,
    assetPredicate: (T) -> Boolean,
  ): Optional<U> =
    chooseRandomAsset(
      remoteAssetManager.supplyRemoteAssetDefinitions()
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

fun <T : AssetDefinition> Collection<T>.filterByCategory(category: MemeAssetCategory): Collection<T> =
  this.filter { it.categories.contains(category) }
