package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.assets.LocalAssetService.hasAssetChanged
import io.unthrottled.amii.assets.LocalStorageService.createDirectories
import io.unthrottled.amii.assets.LocalStorageService.getGlobalAssetDirectory
import io.unthrottled.amii.assets.LocalStorageService.getLocalAssetDirectory
import io.unthrottled.amii.integrations.RestTools
import io.unthrottled.amii.tools.toOptional
import org.apache.commons.io.IOUtils
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Optional

enum class AssetCategory(val category: String) {
  VISUALS("visuals"),
}

object HttpClientFactory {
  fun createHttpClient(): CloseableHttpClient =
    HttpClients.custom()
      .setUserAgent(ApplicationInfo.getInstance()?.fullApplicationName)
      .build()
}

object AssetManager {
  private const val ASSET_SOURCE = "https://waifu.assets.unthrottled.io"

  private val httpClient = HttpClientFactory.createHttpClient()
  private val log = Logger.getInstance(this::class.java)

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun resolveAssetUrl(assetCategory: AssetCategory, assetPath: String): Optional<URI> =
    cachedResolve(assetCategory, assetPath)

  /**
   * Works just like <code>resolveAssetUrl</code> except that it will always
   * download the remote asset.
   */
  fun forceResolveAssetUrl(assetCategory: AssetCategory, assetPath: String): Optional<URI> =
    forceResolve(assetCategory, assetPath)

  private fun cachedResolve(
    assetCategory: AssetCategory,
    assetPath: String,
  ): Optional<URI> =
    resolveAsset(assetCategory, assetPath) { localAssetPath, remoteAssetUrl ->
      resolveTheAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun forceResolve(
    assetCategory: AssetCategory,
    assetPath: String,
  ): Optional<URI> =
    resolveAsset(assetCategory, assetPath) { localAssetPath, remoteAssetUrl ->
      downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun resolveAsset(
    assetCategory: AssetCategory,
    assetPath: String,
    resolveAsset: (Path, String) -> Optional<URI>
  ): Optional<URI> =
    constructLocalAssetPath(assetCategory, assetPath)
      .toOptional()
      .flatMap {
        val remoteAssetUrl = constructRemoteAssetUrl(
          assetCategory,
          assetPath
        )
        resolveAsset(it, remoteAssetUrl)
      }

  private fun constructRemoteAssetUrl(
    assetCategory: AssetCategory,
    assetPath: String,
  ): String = "$ASSET_SOURCE/${assetCategory.category}/$assetPath"

  private fun resolveTheAssetUrl(localAssetPath: Path, remoteAssetUrl: String): Optional<URI> =
    when {
      hasAssetChanged(localAssetPath, remoteAssetUrl) ->
        downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
      Files.exists(localAssetPath) ->
        localAssetPath.toUri().toOptional()
      else -> Optional.empty()
    }

  fun constructLocalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Path =
    Paths.get(
      getLocalAssetDirectory(),
      assetCategory.category,
      assetPath
    ).normalize().toAbsolutePath()

  fun constructGlobalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Optional<Path> =
    getGlobalAssetDirectory()
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
    createDirectories(localAssetPath)
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
