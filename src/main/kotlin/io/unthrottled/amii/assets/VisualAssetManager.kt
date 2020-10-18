package io.unthrottled.amii.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.assets.AssetCategory.VISUALS
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object VisualAssetManager : RemoteAssetManager<VisualMemeAssetDefinition, VisualMemeAsset>(
  VISUALS
) {
  private val log: Logger? = Logger.getInstance(this::class.java)

  fun supplyListOfAllCharacters(): Set<String> =
    super.supplyAllAssetDefinitions()
      .mapNotNull { it.characters }
      .flatten().toSet()

  override fun convertToAsset(
    asset: VisualMemeAssetDefinition,
    assetUrl: String
  ): VisualMemeAsset =
    asset.toAsset(assetUrl)

  override fun convertToDefinitions(defJson: String): Optional<List<VisualMemeAssetDefinition>> =
    runSafelyWithResult({
      Gson().fromJson<List<VisualMemeAssetDefinition>>(
        defJson,
        object : TypeToken<List<VisualMemeAssetDefinition>>() {}.type
      ).toOptional()
    }) {
      log?.warn("Unable to read Visual Assets for reasons $defJson", it)
      Optional.empty()
    }
}
