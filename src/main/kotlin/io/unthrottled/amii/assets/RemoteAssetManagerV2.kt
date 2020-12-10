package io.unthrottled.amii.assets

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.exists
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.tools.doOrElse
import java.net.URI
import java.util.Optional

abstract class RemoteAssetManagerV2<T : AssetDefinition, U : Asset>(
  private val assetCategory: AssetCategory
) : HasStatus {
  private lateinit var remoteAndLocalAssets: List<T>
  private lateinit var localAssets: MutableSet<T>

  override var status = Status.UNKNOWN
  private val log = Logger.getInstance(this::class.java)

  init {
    initializeAssetCaches(APIAssetManager.resolveAssetUrl("assets/$assetCategory"))
    LifeCycleManager.registerUpdateListener {
      initializeAssetCaches(
        APIAssetManager.forceResolveAssetUrl("assets/$assetCategory"),
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
          AssetManager.constructLocalAssetPath(assetCategory, asset.path).exists()
        }.toSet().toMutableSet()
      }) {
        if (breakOnFailure) {
          status = Status.BROKEN
          remoteAndLocalAssets = listOf()
          localAssets = mutableSetOf()
        }
      }
  }

  open fun supplyPreferredLocalAssetDefinitions(): Set<T> =
    supplyAllLocalAssetDefinitions()

  open fun supplyPreferredRemoteAssetDefinitions(): List<T> =
    supplyAllRemoteAssetDefinitions()

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
    AssetManager.resolveAssetUrl(assetCategory, asset.path)
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
}
