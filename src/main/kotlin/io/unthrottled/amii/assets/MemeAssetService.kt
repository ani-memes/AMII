package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.toOptional
import java.util.Optional
import kotlin.random.Random

class MemeAsset(
  val visualMemeAsset: VisualMemeContent,
  val audibleAsset: AudibleMemeContent? = null,
)

object MemeAssetService {

  private val ranbo = Random(System.currentTimeMillis())

  fun getFromCategory(category: MemeAssetCategory): Optional<MemeAsset> =
    when (category) {
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
      MemeAssetCategory.ACKNOWLEDGEMENT
      -> pickRandomAssetByCategory(
        category
      )
      else -> throw NotImplementedError("You can't use $category here.")
    }

  fun pickFromCategories(vararg categories: MemeAssetCategory): Optional<MemeAsset> = categories.toOptional()
    .filter { it.isNotEmpty() }
    .flatMap { getFromCategory(it.random(ranbo)) }

  // todo: revisit
  private fun pickRandomAssetByCategory(category: MemeAssetCategory): Optional<MemeAsset> =
    VisualAssetDefinitionService.getRandomAssetByCategory(category)
      .flatMap { visualMemeAsset ->
        if (visualMemeAsset.audioId != null) {
          AudibleAssetDefinitionService.getAssetById(visualMemeAsset.audioId)
            .map { audibleAsset -> MemeAsset(visualMemeAsset, audibleAsset) }
        } else {
          MemeAsset(visualMemeAsset).toOptional()
        }
      }
}
