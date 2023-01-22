package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.messages.Topic
import io.unthrottled.amii.assets.ContentAssetManager.constructLocalContentPath
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.platform.UpdateAssetsListener
import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.doOrElse
import java.io.InputStream
import java.net.URI
import java.util.Optional
import kotlin.io.path.exists

enum class Status {
  OK, BROKEN, UNKNOWN
}

interface HasStatus {
  var status: Status
}

fun interface ContentManagerListener {
  companion object {
    val TOPIC: Topic<ContentManagerListener> =
      Topic(ContentManagerListener::class.java)
  }

  fun onUpdate(assetCategory: AssetCategory)
}

abstract class RemoteContentManager<T : ContentRepresentation, U : Content>(
  private val assetCategory: AssetCategory,
) : HasStatus {
  private var remoteAndLocalAssets: List<T> = listOf()
  private var localAssets: MutableSet<T> = mutableSetOf()

  override var status = Status.UNKNOWN
  private val log = Logger.getInstance(this::class.java)

  init {
    val apiPath = "assets/${assetCategory.category}"
    cachedInitialization(apiPath)
    LifeCycleManager.registerAssetUpdateListener(object : UpdateAssetsListener {
      override fun onRequestedUpdate() {
        ExecutionService.executeAsynchronously {
          initializeAssetCaches(
            APIAssetManager.forceResolveAssetUrl(apiPath),
            breakOnFailure = false
          )
        }
      }

      override fun onRequestedBackgroundUpdate() {
        cachedInitialization(apiPath)
      }
    })
  }

  private fun cachedInitialization(apiPath: String) {
    initializeAssetCaches(
      APIAssetManager.resolveAssetUrl(apiPath) {
        convertToDefinitions(it)
      }
    )
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
    ApplicationManager.getApplication().messageBus.syncPublisher(ContentManagerListener.TOPIC)
      .onUpdate(assetCategory)
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
