package io.unthrottled.amii.assets

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import kotlin.random.Random

class MemeAsset(
  val visualMemeContent: VisualMemeContent,
  val audibleMemeContent: AudibleContent? = null,
)

object MemeAssetService: Logging {

  private val ranbo = Random(System.currentTimeMillis())

  private val allowedCategories = setOf(
    MemeAssetCategory.CELEBRATION,
    MemeAssetCategory.DISAPPOINTMENT,
    MemeAssetCategory.SHOCKED,
    MemeAssetCategory.SMUG,
    MemeAssetCategory.WAITING,
    MemeAssetCategory.WELCOMING,
    MemeAssetCategory.ENRAGED,
    MemeAssetCategory.FRUSTRATION,
    MemeAssetCategory.HAPPY,
    MemeAssetCategory.MOCKING,
    MemeAssetCategory.MOTIVATION,
    MemeAssetCategory.ACKNOWLEDGEMENT,
    MemeAssetCategory.ALERT,
    MemeAssetCategory.PATIENTLY_WAITING,
    MemeAssetCategory.BORED,
    MemeAssetCategory.TIRED,
    MemeAssetCategory.POUTING,
  )

  fun getFromCategory(category: MemeAssetCategory): Optional<MemeAsset> =
    when (category) {
      in allowedCategories -> pickRandomAssetByCategory(
        category
      )
      else -> throw NotImplementedError("You can't use $category here.")
    }

  fun pickFromCategories(vararg categories: MemeAssetCategory): Optional<MemeAsset> = categories.toOptional()
    .filter { it.isNotEmpty() }
    .flatMap { getFromCategory(it.random(ranbo)) }

  private fun pickRandomAssetByCategory(category: MemeAssetCategory): Optional<MemeAsset> =
    VisualAssetDefinitionService.getRandomAssetByCategory(category)
      .flatMap { visualMemeAsset ->
        if (Config.instance.soundEnabled && !visualMemeAsset.audioId.isNullOrBlank()) {
          AudibleAssetDefinitionService.getAssetById(visualMemeAsset.audioId)
            .map { audibleAsset -> MemeAsset(visualMemeAsset, audibleAsset) }
            .or {
              logger().warn("Unable to find audible asset ${visualMemeAsset.audioId} for asset ${visualMemeAsset.id}")
              Optional.empty()
            }
        } else {
          MemeAsset(visualMemeAsset).toOptional()
        }
      }
}
