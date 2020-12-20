package io.unthrottled.amii.assets

import io.unthrottled.amii.integrations.RestTools
import java.io.InputStream
import java.util.Optional

object AssetAPI {
  private val API_URL = System.getenv().getOrDefault(
    "API_URL", "https://amii.api.unthrottled.io/public/"
  )


  fun <T> getAsset(
    path: String,
    bodyExtractor: (InputStream) -> T
  ): Optional<T> = RestTools.performRequest(
    "$API_URL$path",
    bodyExtractor
  )
}
