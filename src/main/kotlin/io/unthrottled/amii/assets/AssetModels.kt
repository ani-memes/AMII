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

interface AssetV2

interface AssetDefinitionV2 {
  val id: String
  val path: String
}

data class VisualMemeAssetV2(
  val filePath: URI,
  val imageAlt: String,
  val audioId: String?,
) : AssetV2

data class VisualMemeAssetDefinitionV2(
  override val id: String,
  override val path: String,
  val alt: String,
  val cat: List<Int>,
  val aud: String? = null,
) : AssetDefinitionV2 {

  fun toAsset(assetUrl: URI): VisualMemeAssetV2 =
    VisualMemeAssetV2(
      assetUrl,
      alt,
      aud,
    )
}

data class AudibleAssetDefinition(
  override val id: String,
  override val path: String
) : AssetDefinitionV2 {
  fun toAsset(assetUrl: URI): AudibleMemeAsset =
    AudibleMemeAsset(
      assetUrl,
    )
}

data class AudibleMemeAsset(
  val filePath: URI
) : AssetV2

data class AnimeAsset(
  val id: String,
  val name: String,
)

data class CharacterAsset(
  val id: String,
  val animeId: String,
  val name: String,
  val gender: Int,
)
