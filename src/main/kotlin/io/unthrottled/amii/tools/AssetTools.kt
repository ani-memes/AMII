package io.unthrottled.amii.tools

import com.intellij.openapi.util.text.StringUtil
import io.unthrottled.amii.assets.MemeAsset
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.assets.MemeAssetService
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional

object AssetTools {

  private const val MAXIMUM_RETRY_ATTEMPTS = 6

  @JvmStatic
  fun calculateMD5Hash(path: Path): String {
    return computeCheckSum(Files.readAllBytes(path))
  }

  private fun computeCheckSum(byteArray: ByteArray): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(byteArray)
    return StringUtil.toHexString(messageDigest.digest())
  }

  fun resolveAssetFromCategories(
    vararg categories: MemeAssetCategory
  ): Optional<MemeAsset> {
    return attemptToGetMemeAssetFromCategories(
      0,
      *categories
    )
  }

  private fun attemptToGetMemeAssetFromCategories(
    attempts: Int,
    vararg categories: MemeAssetCategory
  ): Optional<MemeAsset> {
    return if (attempts < MAXIMUM_RETRY_ATTEMPTS) {
      MemeAssetService.pickFromCategories(
        *categories
      ).map { it.toOptional() }
        .orElseGet {
          attemptToGetMemeAssetFromCategories(
            attempts + 1,
            *categories
          )
        }
    } else {
      Optional.empty()
    }
  }
}
