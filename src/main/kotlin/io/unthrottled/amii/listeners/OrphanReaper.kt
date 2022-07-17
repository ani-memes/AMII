package io.unthrottled.amii.listeners

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.assets.APIAssetListener
import io.unthrottled.amii.assets.AssetCategory.AUDIBLE
import io.unthrottled.amii.assets.AssetCategory.VISUALS
import io.unthrottled.amii.assets.AudibleContentManager
import io.unthrottled.amii.assets.Content
import io.unthrottled.amii.assets.ContentRepresentation
import io.unthrottled.amii.assets.LocalStorageService
import io.unthrottled.amii.assets.RemoteContentManager
import io.unthrottled.amii.assets.RemoteVisualContentManager
import io.unthrottled.amii.assets.VisualContentManager
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafely
import io.unthrottled.amii.tools.toOptional
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Optional

class OrphanReaper : APIAssetListener, Logging {

  override fun onDownload(apiPath: String) {
    harvestOrphans(apiPath)
  }

  override fun onUpdate(apiPath: String) {
    harvestOrphans(apiPath)
  }

  private fun harvestOrphans(apiPath: String) {
    ApplicationManager.getApplication()
      .executeOnPooledThread {
        when (apiPath.substringAfterLast("/").toLowerCase()) {
          VISUALS.category -> VISUALS.toOptional()
          AUDIBLE.category -> AUDIBLE.toOptional()
          else -> Optional.empty()
        }.ifPresent { assetCategory ->
          val assetManager: RemoteContentManager<out ContentRepresentation, out Content> = when (assetCategory) {
            VISUALS -> RemoteVisualContentManager
            else -> AudibleContentManager
          }
          val activePaths = assetManager.supplyAllAssetDefinitions()
            .map { it.path }.toSet()

          val localStoragePath = LocalStorageService.getContentDirectory()
          val assetDirectory = Paths.get(localStoragePath, assetCategory.category)
          if (Files.exists(assetDirectory)) {
            val parentDirectoryLength = assetDirectory.toAbsolutePath().toString().length + 1
            Files.walk(assetDirectory)
              .filter { Files.isRegularFile(it) }
              .filter {
                activePaths.contains(
                  it.toString().substring(parentDirectoryLength)
                ).not()
              }.forEach { orphanedBinary ->
                logger().warn("Harvesting orphaned binary $orphanedBinary")
                runSafely({
                  Files.delete(orphanedBinary)
                }) {
                  logger().warn("Harvesting orphaned binary $it failed for reasons", it)
                }
              }
          }
        }
      }
  }
}
