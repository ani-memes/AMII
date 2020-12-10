package io.unthrottled.amii.assets

import io.unthrottled.amii.integrations.RestTools
import java.io.InputStream
import java.util.Optional

object AssetAPI {
  private const val API_URL = "http://localhost:4000/public/"

  fun <T> getAsset(
    path: String,
    bodyExtractor: (InputStream) -> T
  ): Optional<T> = RestTools.performRequest(
    "$API_URL$path",
    bodyExtractor
  )
}
