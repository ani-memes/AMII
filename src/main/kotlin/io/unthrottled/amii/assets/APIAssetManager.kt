package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.amii.assets.AssetStatus.NOT_DOWNLOADED
import io.unthrottled.amii.assets.AssetStatus.STALE
import io.unthrottled.amii.assets.LocalContentService.hasAPIAssetChanged
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
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
import java.util.stream.Collectors
import java.util.stream.Stream

interface APIAssetListener {
  companion object {
    val TOPIC: Topic<APIAssetListener> = Topic.create(
      "AMII API Assets",
      APIAssetListener::class.java
    )
  }

  fun onDownload(apiPath: String) {}
  fun onUpdate(apiPath: String) {}
}

object APIAssetManager : Logging {

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun <T : AssetRepresentation> resolveAssetUrl(
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): Optional<URI> =
    cachedResolve(apiPath, assetConverter)

  /**
   * Works just like <code>resolveAssetUrl</code> except that it will always
   * download the remote asset.
   */
  fun forceResolveAssetUrl(
    apiPath: String
  ): Optional<URI> =
    forceResolve(apiPath)

  private fun <T : AssetRepresentation> cachedResolve(
    assetPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): Optional<URI> =
    resolveAsset(assetPath) { localAssetPath, remoteAssetUrl ->
      resolveTheAssetUrl(localAssetPath, remoteAssetUrl, assetConverter)
    }

  private fun forceResolve(
    assetPath: String
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

  private fun <T : AssetRepresentation> resolveTheAssetUrl(
    localAssetPath: Path,
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): Optional<URI> {
    val (apiAssetStatus, metaData) = hasAPIAssetChanged(localAssetPath)
    return when {
      apiAssetStatus == STALE && metaData is Instant ->
        downloadAndUpdateAssetDefinitions(
          localAssetPath,
          "$apiPath?changedSince=${metaData.epochSecond}",
          assetConverter
        ).toOptional()
      apiAssetStatus == NOT_DOWNLOADED ||
        (apiAssetStatus == STALE && metaData == null) -> downloadAndGetAssetUrl(localAssetPath, apiPath)
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
    apiPath: String
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

      ApplicationManager.getApplication().messageBus
        .syncPublisher(APIAssetListener.TOPIC)
        .onDownload(apiPath)

      localAssetPath.toUri()
    }
  }

  private fun <T : AssetRepresentation> downloadAndUpdateAssetDefinitions(
    localAssetPath: Path,
    apiPath: String,
    assetConverter: (InputStream) -> Optional<List<T>>
  ): URI =
    runSafelyWithResult({
      AssetAPI.getAsset(apiPath) { inputStream ->
        assetConverter(inputStream)
      }
        .flatMap { it }
        .flatMap { newAssets ->
          assetConverter(Files.newInputStream(localAssetPath))
            .map { existingAssets -> newAssets to existingAssets }
        }
        .map { (newAssets, existingAssets) ->
          val seenAssets = ConcurrentHashMap.newKeySet<String>()
          val deletedAssetIds = newAssets
            .filter { it.del ?: false }
            .map { it.id }
            .toSet()

          val updatedAssets = Stream.concat(
            newAssets.stream(),
            existingAssets.stream()
          )
            .filter { it.del != true }
            .filter { deletedAssetIds.contains(it.id).not() }
            .filter {
              seenAssets.add(it.id)
            }.filter { it != null }.collect(Collectors.toList())

          Files.newBufferedWriter(
            localAssetPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
          ).use { bufferedWriter ->
            bufferedWriter.write(
              Gson().toJson(updatedAssets)
            )
          }

          ApplicationManager.getApplication().messageBus
            .syncPublisher(APIAssetListener.TOPIC)
            .onUpdate(apiPath)

          localAssetPath.toUri()
        }
        .orElseGet {
          localAssetPath.toUri()
        }
    }) {
      logger().warn("Unable to update asset $apiPath", it)
      localAssetPath.toUri()
    }
}
