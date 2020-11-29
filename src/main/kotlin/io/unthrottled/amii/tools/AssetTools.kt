package io.unthrottled.amii.tools

import io.unthrottled.amii.assets.MemeAsset
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.assets.MemeAssetService
import java.util.Optional

object AssetTools {

  private const val MAXIMUM_RETRY_ATTEMPTS = 6

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
