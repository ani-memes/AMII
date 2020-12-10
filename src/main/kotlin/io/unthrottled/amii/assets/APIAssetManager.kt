package io.unthrottled.amii.assets

import io.unthrottled.amii.assets.AssetStatus.NOT_DOWNLOADED
import io.unthrottled.amii.assets.AssetStatus.STALE
import io.unthrottled.amii.assets.LocalAssetService.hasAPIAssetChanged
import io.unthrottled.amii.tools.toOptional
import org.apache.commons.io.IOUtils
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Optional

object APIAssetManager {

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun resolveAssetUrl(apiPath: String): Optional<URI> =
    cachedResolve(apiPath)

  /**
   * Works just like <code>resolveAssetUrl</code> except that it will always
   * download the remote asset.
   */
  fun forceResolveAssetUrl(apiPath: String): Optional<URI> =
    forceResolve(apiPath)

  private fun cachedResolve(
    assetPath: String,
  ): Optional<URI> =
    resolveAsset(assetPath) { localAssetPath, remoteAssetUrl ->
      resolveTheAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun forceResolve(
    assetPath: String,
  ): Optional<URI> =
    resolveAsset(assetPath) { localAssetPath, remoteAssetUrl ->
      downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun resolveAsset(
    apiPath: String,
    resolveAsset: (Path, String) -> Optional<URI>
  ): Optional<URI> =
    constructGlobalAssetPath(apiPath)
      .orElseGet { constructLocalAssetPath(apiPath) }
      .toOptional()
      .flatMap {
        resolveAsset(it, apiPath)
      }

  private fun resolveTheAssetUrl(localAssetPath: Path, apiPath: String): Optional<URI> {
    val apiAssetStatus = hasAPIAssetChanged(localAssetPath)
    return when {
      apiAssetStatus == STALE ||
        apiAssetStatus == NOT_DOWNLOADED ->
        downloadAndGetAssetUrl(localAssetPath, apiPath)
      Files.exists(localAssetPath) ->
        localAssetPath.toUri().toOptional()
      else -> Optional.empty()
    }
  }

  private fun constructLocalAssetPath(
    assetPath: String
  ): Path =
    Paths.get(
      LocalStorageService.getLocalAssetDirectory(),
      AssetCategory.API.category,
      assetPath
    ).normalize().toAbsolutePath()

  private fun constructGlobalAssetPath(
    assetPath: String
  ): Optional<Path> =
    LocalStorageService.getGlobalAssetDirectory()
      .map {
        Paths.get(
          it,
          AssetCategory.API.category,
          assetPath
        )
      }

  // todo: add changed since date
  private fun downloadAndGetAssetUrl(
    localAssetPath: Path,
    apiPath: String,
  ): Optional<URI> {
    LocalStorageService.createDirectories(localAssetPath)
    return AssetAPI.getAsset(apiPath) { inputStream ->
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
