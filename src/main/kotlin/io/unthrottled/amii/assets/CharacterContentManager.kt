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
import java.util.Optional

object CharacterContentManager : APIContentManager<CharacterRepresentation>(AssetCategory.CHARACTERS), Logging {

  override fun convertToDefinitions(defJson: String): Optional<List<CharacterRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<CharacterRepresentation>>(
        defJson,
        object : TypeToken<List<CharacterRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read audible Assets for reasons $defJson", it)
      Optional.empty()
    }

  override fun convertToDefinitions(defJson: InputStream): Optional<List<CharacterRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<CharacterRepresentation>>(
        JsonReader(InputStreamReader(defJson)),
        object : TypeToken<List<CharacterRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read audible Assets for reasons $defJson", it)
      Optional.empty()
    }
}
