package io.unthrottled.amii.assets

import java.util.UUID

enum class MemeAssetCategory {
  ACKNOWLEDGEMENT,
  FRUSTRATION,
  ENRAGED,
  CELEBRATION,
  HAPPY,
  SMUG,
  WAITING,
  MOTIVATION,
  WELCOMING,
  DEPARTURE,
  ENCOURAGEMENT,
  MOCKING,
  SHOCKED,
  DISAPPOINTMENT
}

interface AssetDefinition {
  val categories: List<MemeAssetCategory>
  val path: String
  val groupId: UUID?
}

interface Asset {
  val groupId: UUID?
}

data class ImageDimension(
  val width: Int,
  val height: Int
)

data class VisualMemeAssetDefinition(
  override val path: String,
  val imageAlt: String,
  val imageDimensions: ImageDimension,
  override val categories: List<MemeAssetCategory>,
  override val groupId: UUID? = null,
  val characters: List<String>? = null
) : AssetDefinition {
  fun toAsset(assetUrl: String): VisualMemeAsset =
    VisualMemeAsset(
      assetUrl,
      imageAlt,
      imageDimensions,
      groupId,
      characters
    )
}

data class VisualMemeAsset(
  val filePath: String,
  val imageAlt: String,
  val imageDimensions: ImageDimension,
  override val groupId: UUID? = null,
  val characters: List<String>? = null
) : Asset
