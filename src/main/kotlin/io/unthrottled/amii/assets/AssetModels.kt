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

// representation == REST API model
interface AssetRepresentation {
  val id: String
}

// content == has something to download
interface ContentRepresentation : AssetRepresentation {
  val path: String
}

data class VisualMemeContent(
  val filePath: URI,
  val imageAlt: String,
  val audioId: String?,
) : Content

data class VisualAssetEntity(
  val id: String,
  val path: String, // has to be downloaded separately
  val alt: String,
  val assetCategory: MemeAssetCategory,
  val characters: List<AnimeCharacter>, // should already be downloaded at startup
  val audibleAssetId: String? = null, // has to be downloaded separately
) {
  fun toContent(assetUrl: URI): VisualMemeContent =
    VisualMemeContent(
      assetUrl,
      alt,
      audibleAssetId,
    )
}

data class VisualAssetRepresentation(
  override val id: String,
  override val path: String,
  val alt: String,
  val cat: List<Int>,
  val char: List<String>,
  val aud: String? = null,
) : ContentRepresentation

data class AudibleRepresentation(
  override val id: String,
  override val path: String
) : ContentRepresentation {
  fun toContent(assetUrl: URI): AudibleContent =
    AudibleContent(
      assetUrl,
    )
}

data class AudibleContent(
  val filePath: URI
) : Content

data class AnimeRepresentation(
  override val id: String,
  val name: String,
) : AssetRepresentation

data class Anime(
  val id: String,
  val name: String,
)

@Suppress("MagicNumber")
enum class Gender(val value: Int) {
  FEMALE(0),
  MALE(1),
  YES(2),
  APACHE_ATTACK_HELICOPTER(3);

  companion object {
    private val mappedGenders = values().map { it.value to it }.toMap()

    fun fromValue(value: Int): Gender =
      mappedGenders[value] ?: YES
  }
}

data class CharacterRepresentation(
  override val id: String,
  val animeId: String,
  val name: String,
  val gender: Int,
) : AssetRepresentation

data class AnimeCharacter(
  val id: String,
  val anime: Anime,
  val name: String,
  val gender: Gender,
)
