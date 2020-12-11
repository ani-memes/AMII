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

interface Content

interface AssetDefinition {
  val id: String
  val path: String
}

data class VisualMemeContent(
  val filePath: URI,
  val imageAlt: String,
  val audioId: String?,
) : Content

data class VisualMemeAssetDefinition(
  override val id: String,
  override val path: String,
  val alt: String,
  val cat: List<Int>,
  val aud: String? = null,
) : AssetDefinition {

  fun toContent(assetUrl: URI): VisualMemeContent =
    VisualMemeContent(
      assetUrl,
      alt,
      aud,
    )
}

data class AudibleAssetDefinition(
  override val id: String,
  override val path: String
) : AssetDefinition {
  fun toContent(assetUrl: URI): AudibleMemeContent =
    AudibleMemeContent(
      assetUrl,
    )
}

data class AudibleMemeContent(
  val filePath: URI
) : Content

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
