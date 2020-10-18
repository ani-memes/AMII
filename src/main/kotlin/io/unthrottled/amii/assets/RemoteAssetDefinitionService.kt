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
    category: MemeAssetCategory
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
    memeAssetCategory: MemeAssetCategory
  ): Optional<U> =
    pickRandomAsset(
      remoteAssetManager.supplyAllAssetDefinitions()
        .filterByCategory(memeAssetCategory)
    )
      .map {
        resolveAsset(memeAssetCategory, it)
      }.orElseGet {
        fetchRemoteAsset(memeAssetCategory)
      }

  private fun resolveAsset(
    memeAssetCategory: MemeAssetCategory,
    it: T
  ): Optional<U> {
    return remoteAssetManager.resolveAsset(it)
  }

  private fun fetchRemoteAsset(
    memeAssetCategory: MemeAssetCategory
  ): Optional<U> =
    pickRandomAsset(
      remoteAssetManager.supplyRemoteAssetDefinitions()
        .filterByCategory(memeAssetCategory)
        .ifEmpty {
          remoteAssetManager.supplyAllRemoteAssetDefinitions()
            .filterByCategory(memeAssetCategory)
        }
    ).flatMap { remoteAssetManager.resolveAsset(it) }

  private fun pickRandomAsset(
    assetDefinitions: Collection<T>
  ): Optional<T> =
    assetDefinitions
      .toOptional()
      .filter { it.isNotEmpty() }
      .map { it.random(random) }
}

fun <T : AssetDefinition> Collection<T>.filterByCategory(category: MemeAssetCategory): Collection<T> =
  this.filter { it.categories.contains(category) }
