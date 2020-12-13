package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.Optional

object AudibleContentManager :
  RemoteContentManager<AudibleRepresentation, AudibleContent>(
    AssetCategory.AUDIBLE
  ),
  Logging {

  override fun convertToAsset(
    asset: AudibleRepresentation,
    assetUrl: URI
  ): AudibleContent =
    asset.toContent(assetUrl)

  override fun convertToDefinitions(defJson: String): Optional<List<AudibleRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<AudibleRepresentation>>(
        defJson,
        object : TypeToken<List<AudibleRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read audible Assets for reasons $defJson", it)
      Optional.empty()
    }

  override fun convertToDefinitions(defJson: InputStream): Optional<List<AudibleRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<AudibleRepresentation>>(
        defJson.use {
          JsonReader(InputStreamReader(it))
        },
        object : TypeToken<List<AudibleRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read audible Assets from stream for reasons", it)
      Optional.empty()
    }
}
