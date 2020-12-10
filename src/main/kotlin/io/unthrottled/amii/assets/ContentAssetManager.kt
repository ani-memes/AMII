package io.unthrottled.amii.assets

import io.unthrottled.amii.integrations.RestTools
import io.unthrottled.amii.tools.toOptional
import org.apache.commons.io.IOUtils
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Optional

object ContentAssetManager {
  private const val ASSET_SOURCE = "https://waifu.assets.unthrottled.io"

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun resolveAssetUrl(assetCategory: AssetCategory, assetPath: String): Optional<URI> =
    constructGlobalAssetPath(assetCategory, assetPath)
      .orElseGet { constructLocalAssetPath(assetCategory, assetPath) }
      .toOptional()
      .flatMap {
        val remoteAssetUrl = constructRemoteAssetUrl(
          assetCategory,
          assetPath
        )
        resolveTheAssetUrl(it, remoteAssetUrl)
      }

  private fun constructRemoteAssetUrl(
    assetCategory: AssetCategory,
    assetPath: String,
  ): String = "$ASSET_SOURCE/${assetCategory.category}/$assetPath"

  private fun resolveTheAssetUrl(localAssetPath: Path, remoteAssetUrl: String): Optional<URI> =
    when {
      LocalAssetService.hasAssetChanged(localAssetPath, remoteAssetUrl) ->
        downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
      Files.exists(localAssetPath) ->
        localAssetPath.toUri().toOptional()
      else -> Optional.empty()
    }

  private fun constructLocalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Path =
    Paths.get(
      LocalStorageService.getLocalAssetDirectory(),
      assetCategory.category,
      assetPath
    ).normalize().toAbsolutePath()

  private fun constructGlobalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Optional<Path> =
    LocalStorageService.getGlobalAssetDirectory()
      .map {
        Paths.get(
          it,
          assetCategory.category,
          assetPath
        )
      }

  private fun downloadAndGetAssetUrl(
    localAssetPath: Path,
    remoteAssetUrl: String
  ): Optional<URI> {
    LocalStorageService.createDirectories(localAssetPath)
    return RestTools.performRequest(remoteAssetUrl) { inputStream ->
      Files.newOutputStream(
        localAssetPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use { bufferedWriter ->
        IOUtils.copy(inputStream, bufferedWriter)
      }
      localAssetPath.toUri()
    }
  }
}
