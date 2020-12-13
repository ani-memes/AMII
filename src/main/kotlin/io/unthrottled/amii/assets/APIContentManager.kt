package io.unthrottled.amii.assets

import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.assets.LocalStorageService.readLocalFile
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.tools.doOrElse
import java.io.InputStream
import java.net.URI
import java.util.Optional

abstract class APIContentManager<T : AssetRepresentation>(
  assetCategory: APIAssets,
) : HasStatus {
  private lateinit var remoteAndLocalAssets: List<T>

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
      }) {
        if (breakOnFailure) {
          status = Status.BROKEN
          remoteAndLocalAssets = listOf()
        }
      }
  }

  fun supplyAssets(): List<T> =
    remoteAndLocalAssets

  private fun initializeRemoteAssets(assetUrl: URI): Optional<List<T>> =
    try {
      readLocalFile(assetUrl)
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
