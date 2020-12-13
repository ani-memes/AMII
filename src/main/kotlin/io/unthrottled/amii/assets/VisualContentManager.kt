package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.unthrottled.amii.services.CharacterGatekeeper
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.Collections
import java.util.Optional

object VisualContentManager :
  RemoteContentManager<VisualAssetRepresentation, VisualMemeContent>(
    AssetCategory.VISUALS
  ),
  Logging {

  fun supplyPreferredLocalAssetDefinitions(): List<VisualAssetRepresentation> =
    supplyAllLocalAssetDefinitions()
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(Collections.emptyList()) } // todo: this

  fun supplyPreferredGenderLocalAssetDefinitions(): List<VisualAssetRepresentation> =
    supplyAllLocalAssetDefinitions()
      .filter { CharacterGatekeeper.instance.hasPreferredGender(Collections.emptyList()) } // todo: this

  fun supplyPreferredRemoteAssetDefinitions(): List<VisualAssetRepresentation> =
    supplyAllRemoteAssetDefinitions()
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(Collections.emptyList()) } // todo: this

  fun supplyPreferredGenderRemoteAssetDefinitions(): List<VisualAssetRepresentation> =
    supplyAllRemoteAssetDefinitions()
      .filter { CharacterGatekeeper.instance.hasPreferredGender(Collections.emptyList()) } // todo: this

  override fun convertToAsset(
    asset: VisualAssetRepresentation,
    assetUrl: URI
  ): VisualMemeContent =
    VisualMemeContent(assetUrl, asset.alt, asset.aud)

  override fun convertToDefinitions(defJson: String): Optional<List<VisualAssetRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualAssetRepresentation>>(
        defJson,
        object : TypeToken<List<VisualAssetRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }

  override fun convertToDefinitions(defJson: InputStream): Optional<List<VisualAssetRepresentation>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualAssetRepresentation>>(
        defJson.use {
          JsonReader(InputStreamReader(it))
        },
        object : TypeToken<List<VisualAssetRepresentation>>() {}.type
      ).toOptional()
    }) {
      logger().warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }
}
