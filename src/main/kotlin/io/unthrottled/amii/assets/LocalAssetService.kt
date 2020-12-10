package io.unthrottled.amii.assets

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import io.unthrottled.amii.integrations.RestClient
import io.unthrottled.amii.tools.toOptional
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

private enum class AssetChangedStatus {
  SAME, DIFFERENT, LUL_DUNNO
}

enum class AssetStatus {
  NOT_DOWNLOADED, STALE, CURRENT
}

// todo: don't do dis
@Suppress("TooManyFunctions")
object LocalAssetService {
  private val log = Logger.getInstance(this::class.java)
  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val assetChecks: ConcurrentHashMap<String, Instant> = readPreviousAssetChecks()

  fun hasAssetChanged(
    localInstallPath: Path,
    remoteAssetUrl: String
  ): Boolean =
    !Files.exists(localInstallPath) ||
      (
        !hasBeenCheckedToday(localInstallPath) &&
          isLocalDifferentFromRemote(localInstallPath, remoteAssetUrl) == AssetChangedStatus.DIFFERENT
        )

  fun hasAPIAssetChanged(
    localInstallPath: Path,
  ): AssetStatus =
    when {
      !Files.exists(localInstallPath) -> AssetStatus.NOT_DOWNLOADED
      !hasBeenCheckedToday(localInstallPath) -> AssetStatus.STALE
      else -> AssetStatus.CURRENT
    }

  private fun getOnDiskCheckSum(localAssetPath: Path): String =
    computeCheckSum(Files.readAllBytes(localAssetPath))

  private fun computeCheckSum(byteArray: ByteArray): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(byteArray)
    return StringUtil.toHexString(messageDigest.digest())
  }

  private fun getRemoteAssetChecksum(remoteAssetUrl: String): Optional<String> =
    RestClient.performGet("$remoteAssetUrl.checksum.txt")

  private fun isLocalDifferentFromRemote(
    localInstallPath: Path,
    remoteAssetUrl: String
  ): AssetChangedStatus =
    getRemoteAssetChecksum(remoteAssetUrl)
      .map {
        writeCheckedDate(localInstallPath)
        val onDiskCheckSum = getOnDiskCheckSum(localInstallPath)
        if (it == onDiskCheckSum) {
          AssetChangedStatus.SAME
        } else {
          log.warn(
            """
                      Local asset: $localInstallPath
                      is different from remote asset $remoteAssetUrl
                      Local Checksum: $onDiskCheckSum
                      Remote Checksum: $it
            """.trimIndent()
          )
          AssetChangedStatus.DIFFERENT
        }
      }.orElseGet { AssetChangedStatus.LUL_DUNNO }

  private fun hasBeenCheckedToday(localInstallPath: Path): Boolean =
    assetChecks[getAssetCheckKey(localInstallPath)]?.truncatedTo(ChronoUnit.DAYS) ==
      Instant.now().truncatedTo(ChronoUnit.DAYS)

  private fun writeCheckedDate(localInstallPath: Path) {
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

  private fun getAssetChecksFile() =
    Paths.get(LocalStorageService.getLocalAssetDirectory(), "assetChecks.json")

  private fun getAssetCheckKey(localInstallPath: Path) =
    localInstallPath.toAbsolutePath().toString()
}
