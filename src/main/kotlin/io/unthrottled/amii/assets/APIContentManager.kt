package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.assets.LocalStorageService.readLocalFile
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.doOrElse
import java.io.InputStream
import java.net.URI
import java.util.Optional

// todo: post mvp: consolidate
abstract class APIContentManager<T : AssetRepresentation>(
  private val assetCategory: AssetCategory,
) : HasStatus {
  private lateinit var assetRepresentations: List<T>

  override var status = Status.UNKNOWN
  private val log = Logger.getInstance(this::class.java)

  init {
    val apiPath = "assets/${assetCategory.category}"
    initializeAssetCaches(
      APIAssetManager.resolveAssetUrl(apiPath) {
        convertToDefinitions(it)
      }
    )
    LifeCycleManager.registerAssetUpdateListener {
      ExecutionService.executeAsynchronously {
        initializeAssetCaches(
          APIAssetManager.forceResolveAssetUrl(apiPath),
          breakOnFailure = false
        )
      }
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
        assetRepresentations = allAssetDefinitions
      }) {
        if (breakOnFailure) {
          status = Status.BROKEN
          assetRepresentations = listOf()
        }
      }
    ApplicationManager.getApplication().messageBus.syncPublisher(ContentManagerListener.TOPIC)
      .onUpdate(assetCategory)
  }

  fun supplyAssets(): List<T> =
    assetRepresentations

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
