package io.unthrottled.amii.assets

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.exists
import io.unthrottled.amii.assets.ContentAssetManager.constructLocalContentPath
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.tools.doOrElse
import java.io.InputStream
import java.net.URI
import java.util.Optional

enum class Status {
  OK, BROKEN, UNKNOWN
}

interface HasStatus {
  var status: Status
}

abstract class RemoteContentManager<T : ContentRepresentation, U : Content>(
  private val assetCategory: AssetCategory,
) : HasStatus {
  private lateinit var remoteAndLocalAssets: List<T>
  private lateinit var localAssets: MutableSet<T>

  override var status = Status.UNKNOWN
  private val log = Logger.getInstance(this::class.java)

  init {
    val apiPath = "assets/${assetCategory.category}"
    initializeAssetCaches(
      APIAssetManager.resolveAssetUrl(apiPath) {
        convertToDefinitions(it)
      }
    )
    LifeCycleManager.registerUpdateListener {
      initializeAssetCaches(
        APIAssetManager.forceResolveAssetUrl(apiPath),
        breakOnFailure = false
      )
    }
  }

  private fun initializeAssetCaches(
    assetFileUrl: Optional<URI>,
    breakOnFailure: Boolean = true
  ) {
    assetFileUrl
      .flatMap { assetUrl -> initializeRemoteAssets(assetUrl) }
      .doOrElse({ allAssetDefinitions ->
        status = Status.OK
        remoteAndLocalAssets = allAssetDefinitions
        localAssets = allAssetDefinitions.filter { asset ->
          constructLocalContentPath(assetCategory, asset.path).exists()
        }.toSet().toMutableSet()
      }) {
        if (breakOnFailure) {
          status = Status.BROKEN
          remoteAndLocalAssets = listOf()
          localAssets = mutableSetOf()
        }
      }
  }

  fun supplyAllLocalAssetDefinitions(): Set<T> =
    localAssets

  fun supplyAllRemoteAssetDefinitions(): List<T> =
    remoteAndLocalAssets.filterNot { remoteOrLocalAsset ->
      localAssets.contains(remoteOrLocalAsset)
    }

  fun supplyAllAssetDefinitions(): List<T> =
    remoteAndLocalAssets

  abstract fun convertToAsset(asset: T, assetUrl: URI): U

  fun resolveAsset(asset: T): Optional<U> =
    ContentAssetManager.resolveAssetUrl(assetCategory, asset.path)
      .map { assetUrl ->
        localAssets.add(asset)
        convertToAsset(asset, assetUrl)
      }

  private fun initializeRemoteAssets(assetUrl: URI): Optional<List<T>> =
    try {
      LocalStorageService.readLocalFile(assetUrl)
        .flatMap {
          convertToDefinitions(it)
        }
    } catch (e: Throwable) {
      log.error("Unable to initialize asset metadata.", e)
      Optional.empty()
    }

  abstract fun convertToDefinitions(defJson: String): Optional<List<T>>

  abstract fun convertToDefinitions(defJson: InputStream): Optional<List<T>>
}
