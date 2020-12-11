package io.unthrottled.amii.assets

import java.net.URI

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
