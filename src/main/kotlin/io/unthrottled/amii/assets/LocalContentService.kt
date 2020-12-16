package io.unthrottled.amii.assets

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import io.unthrottled.amii.assets.AssetCheckService.getCheckedDate
import io.unthrottled.amii.assets.AssetCheckService.hasBeenCheckedToday
import io.unthrottled.amii.assets.AssetCheckService.writeCheckedDate
import io.unthrottled.amii.integrations.RestClient
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional

private enum class AssetChangedStatus {
  SAME, DIFFERENT, LUL_DUNNO
}

enum class AssetStatus {
  NOT_DOWNLOADED, STALE, CURRENT
}

data class AssetCheckPayload(
  val status: AssetStatus,
  val metaData: Any? = null,
)

object LocalContentService {
  private val log = Logger.getInstance(this::class.java)

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
  ): AssetCheckPayload =
    when {
      !Files.exists(localInstallPath) -> AssetCheckPayload(AssetStatus.NOT_DOWNLOADED)
      !hasBeenCheckedToday(localInstallPath) -> {
        val metaData = getCheckedDate(localInstallPath)
        writeCheckedDate(localInstallPath)
        AssetCheckPayload(
          AssetStatus.STALE,
          metaData
        )
      }
      else -> AssetCheckPayload(AssetStatus.CURRENT)
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
}
