package io.unthrottled.amii.assets

import com.google.gson.Gson
import io.unthrottled.amii.assets.AssetStatus.NOT_DOWNLOADED
import io.unthrottled.amii.assets.AssetStatus.STALE
import io.unthrottled.amii.assets.LocalContentService.hasAPIAssetChanged
import io.unthrottled.amii.tools.toList
import io.unthrottled.amii.tools.toOptional
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

object APIAssetManager {

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun <T : AssetDefinition> resolveAssetUrl(
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): Optional<URI> =
    cachedResolve(apiPath, assetConverter)

  /**
   * Works just like <code>resolveAssetUrl</code> except that it will always
   * download the remote asset.
   */
  fun forceResolveAssetUrl(
    apiPath: String,
  ): Optional<URI> =
    forceResolve(apiPath)

  private fun <T : AssetDefinition> cachedResolve(
    assetPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>,
  ): Optional<URI> =
    resolveAsset(assetPath) { localAssetPath, remoteAssetUrl ->
      resolveTheAssetUrl(localAssetPath, remoteAssetUrl, assetConverter)
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
    constructLocalContentStoragePath(apiPath)
      .toOptional()
      .flatMap {
        resolveAsset(it, apiPath)
      }

  private fun <T : AssetDefinition> resolveTheAssetUrl(
    localAssetPath: Path,
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): Optional<URI> {
    val (apiAssetStatus, metaData) = hasAPIAssetChanged(localAssetPath)
    return when {
      apiAssetStatus == STALE -> downloadAndGetAssetUrl(localAssetPath, apiPath)
      apiAssetStatus == NOT_DOWNLOADED && metaData is Instant ->
        downloadAndUpdateAssetDefinitions(
          localAssetPath,
          "$apiPath?changedSince=${metaData.epochSecond}",
          assetConverter,
        ).toOptional()
      Files.exists(localAssetPath) ->
        localAssetPath.toUri().toOptional()
      else -> Optional.empty()
    }
  }

  private fun constructLocalContentStoragePath(
    assetPath: String
  ): Path =
    Paths.get(
      LocalStorageService.getContentDirectory(),
      AssetCategory.API.category,
      assetPath
    ).normalize().toAbsolutePath()

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

  private fun <T : AssetDefinition> downloadAndUpdateAssetDefinitions(
    localAssetPath: Path,
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>,
  ): URI =
    AssetAPI.getAsset(apiPath) { inputStream ->
      inputStream.use {
        assetConverter(it)
      }
    }.flatMap { it }
      .flatMap { newAssets ->
        Files.newInputStream(localAssetPath)
          .use {
            assetConverter(it)
          }.map { existingAssets -> newAssets to existingAssets }
      }
      .map { (newAssets, existingAssets) ->

        val seenAssets = ConcurrentHashMap.newKeySet<String>()
        val updatedAssets = Stream.concat(
          newAssets.stream(),
          existingAssets.stream(),
        ).filter {
          seenAssets.add(it.id)
        }.toList()

        Files.newBufferedWriter(
          localAssetPath,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
        ).use { bufferedWriter ->
          bufferedWriter.write(
            Gson().toJson(updatedAssets)
          )
          localAssetPath.toUri()
        }
      }
      .orElseGet {
        localAssetPath.toUri()
      }
}
