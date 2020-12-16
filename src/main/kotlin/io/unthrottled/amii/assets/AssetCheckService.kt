package io.unthrottled.amii.assets

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.tools.toOptional
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

object AssetCheckService {
  private val log = Logger.getInstance(this::class.java)
  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val assetChecks: ConcurrentHashMap<String, Instant> = readPreviousAssetChecks()

  private fun readPreviousAssetChecks(): ConcurrentHashMap<String, Instant> = try {
    getAssetChecksFile().toOptional()
      .filter { Files.exists(it) }
      .map {
        Files.newBufferedReader(it).use { reader ->
          gson.fromJson<ConcurrentHashMap<String, Instant>>(
            reader,
            object : TypeToken<ConcurrentHashMap<String, Instant>>() {}.type
          )
        }
      }.orElseGet { ConcurrentHashMap() }
  } catch (e: Throwable) {
    log.warn("Unable to get local asset checks for raisins", e)
    ConcurrentHashMap()
  }

  fun getCheckedDate(localInstallPath: Path) = assetChecks[getAssetCheckKey(localInstallPath)]

  fun writeCheckedDate(localInstallPath: Path) {
    assetChecks[getAssetCheckKey(localInstallPath)] = Instant.now()
    val assetCheckPath = getAssetChecksFile()
    LocalStorageService.createDirectories(assetCheckPath)
    Files.newBufferedWriter(
      assetCheckPath,
      Charset.defaultCharset(),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    ).use { writer ->
      writer.write(gson.toJson(assetChecks))
    }
  }

  private fun getAssetChecksFile() =
    Paths.get(LocalStorageService.getContentDirectory(), "assetChecks.json")

  private fun getAssetCheckKey(localInstallPath: Path) =
    localInstallPath.toAbsolutePath().toString()

  fun hasBeenCheckedToday(localInstallPath: Path): Boolean =
    getCheckedDate(localInstallPath)?.truncatedTo(ChronoUnit.DAYS) ==
      Instant.now().truncatedTo(ChronoUnit.DAYS)
}
