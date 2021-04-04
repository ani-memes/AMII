package io.unthrottled.amii.integrations

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import io.unthrottled.amii.integrations.RestTools.performRequest
import io.unthrottled.amii.tools.readAllTheBytes
import io.unthrottled.amii.tools.toOptional
import java.io.InputStream
import java.util.Optional

object RestClient {

  fun performGet(url: String): Optional<String> =
    performRequest(url) { responseBody ->
      String(responseBody.readAllTheBytes())
    }
}

object RestTools {
  private val log = Logger.getInstance(this::class.java)

  fun <T> performRequest(
    url: String,
    bodyExtractor: (InputStream) -> T
  ): Optional<T> {
    log.info("Attempting to download remote asset: $url")
    return HttpRequests.request(url)
      .connect { request ->
        try {
          val body = bodyExtractor(request.inputStream)
          log.info("Asset has responded for remote asset: $url")
          body.toOptional()
        } catch (e: HttpRequests.HttpStatusException) {
          log.warn("Unable to get remote asset: $url for raisins", e)
          Optional.empty<T>()
        }
      }
  }
}
