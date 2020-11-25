package io.unthrottled.amii.assets

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.tools.runSafely
import io.unthrottled.amii.tools.toOptional
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional

object LocalStorageService {
  private val log = Logger.getInstance(this::class.java)
  private const val ASSET_DIRECTORY = "amiiAssets"

  fun readLocalFile(assetUrl: URI): Optional<String> =
    Optional.ofNullable(Files.readAllBytes(Paths.get(assetUrl)))
      .map { String(it, Charsets.UTF_8) }

  fun createDirectories(directoriesToCreate: Path) {
    try {
      Files.createDirectories(directoriesToCreate.parent)
    } catch (e: IOException) {
      log.error("Unable to create directories $directoriesToCreate for raisins", e)
    }
  }

  fun getLocalAssetDirectory(): String =
    getGlobalAssetDirectory()
      .orElseGet {
        Paths.get(
          PathManager.getConfigPath(),
          ASSET_DIRECTORY
        ).toAbsolutePath().toString()
      }

  fun getGlobalAssetDirectory(): Optional<String> =
    Paths.get(
      PathManager.getConfigPath(),
      "..",
      ASSET_DIRECTORY
    ).toAbsolutePath()
      .normalize()
      .toOptional()
      .filter { Files.isWritable(it.parent) }
      .map {
        if (Files.exists(it).not()) {
          runSafely({
            Files.createDirectories(it)
          }) {
            log.warn("Unable to create global directory for raisins", it)
          }
        }
        it.toString()
      }
}
