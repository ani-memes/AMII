package io.unthrottled.amii.assets

import io.unthrottled.amii.assets.VisualEntitySupplier.getLocalAssetsByCategory
import io.unthrottled.amii.assets.VisualEntitySupplier.getPreferredRemoteAssets
import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.toStream
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.min

object BackgroundAssetService {

  private const val MIN_ASSETS_REQUIRED = 4

  fun downloadNewAssets(
    memeAssetCategory: MemeAssetCategory,
  ) {
    ExecutionService.executeAsynchronously {
      getAssetsToDownload(memeAssetCategory)
        .forEach { visualAssetEntity ->
          RemoteVisualContentManager.resolveAsset(visualAssetEntity.representation)
            .map { it.audioId }
            .ifPresent {
              AudibleAssetDefinitionService.getAssetById(it)
            }
        }
    }
  }

  private fun getAssetsToDownload(memeAssetCategory: MemeAssetCategory): Stream<VisualAssetEntity> {
    val localMemeSize = getLocalAssetsByCategory(memeAssetCategory).size
    val needsMore = localMemeSize < MIN_ASSETS_REQUIRED
    val remoteAssets = getPreferredRemoteAssets(memeAssetCategory)
    return if (needsMore && remoteAssets.isNotEmpty()) {
      IntStream.range(0, min(MIN_ASSETS_REQUIRED, remoteAssets.size)).mapToObj {
        chooseAssetAtRandom(remoteAssets)
      }.flatMap {
        it.map { visualAssetEntity -> visualAssetEntity.toStream() }.orElse(Stream.empty())
      }
    } else {
      chooseAssetAtRandom(remoteAssets)
        .map { it.toStream() }
        .orElseGet { Stream.empty() }
    }
  }
}
