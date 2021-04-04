package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.integrations.RestTools
import java.io.InputStream
import java.util.Optional
import java.util.concurrent.Callable

object AssetAPI {
  private val API_URL = System.getenv().getOrDefault(
    "API_URL",
    "https://amii.api.unthrottled.io/public/"
  )

  fun <T> getAsset(
    path: String,
    bodyExtractor: (InputStream) -> T
  ): Optional<T> =
    ApplicationManager.getApplication().executeOnPooledThread(
      Callable {
        RestTools.performRequest(
          "$API_URL$path",
          bodyExtractor
        )
      }
    ).get()
}
