package io.unthrottled.amii.assets

import java.net.URI
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

// todo: resolve characters
data class VisualMemeAssetDefinition(
  override val path: String,
  val imageAlt: String,
  override val categories: List<MemeAssetCategory>,
  override val groupId: UUID? = null,
) : AssetDefinition {
  fun toAsset(assetUrl: URI): VisualMemeAsset =
    VisualMemeAsset(
      assetUrl,
      imageAlt,
      groupId,
    )
}

data class VisualMemeAsset(
  val filePath: URI,
  val imageAlt: String,
  override val groupId: UUID? = null,
) : Asset
