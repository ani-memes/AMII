package io.unthrottled.amii.assets

import io.unthrottled.amii.assets.AssetCheckService.getCheckedDate
import io.unthrottled.amii.assets.AssetCheckService.hasBeenCheckedToday
import io.unthrottled.amii.assets.AssetCheckService.writeCheckedDate
import java.nio.file.Files
import java.nio.file.Path

enum class AssetStatus {
  NOT_DOWNLOADED, STALE, CURRENT
}

data class AssetCheckPayload(
  val status: AssetStatus,
  val metaData: Any? = null
)

object LocalContentService {
  fun hasAssetChanged(
    localInstallPath: Path
  ): Boolean =
    !Files.exists(localInstallPath)

  fun hasAPIAssetChanged(
    localInstallPath: Path
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
}
