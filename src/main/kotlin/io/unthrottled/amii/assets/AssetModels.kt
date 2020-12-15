package io.unthrottled.amii.assets

import java.net.URI

@Suppress("MagicNumber")
enum class MemeAssetCategory(val value: Int) {
  ACKNOWLEDGEMENT(0),
  FRUSTRATION(1),
  ENRAGED(2),
  CELEBRATION(3),
  HAPPY(4),
  SMUG(5),
  WAITING(6),
  MOTIVATION(7),
  WELCOMING(8),
  DEPARTURE(9),
  ENCOURAGEMENT(10),
  MOCKING(11),
  SHOCKED(12),
  DISAPPOINTMENT(13);

  companion object {
    private val mappedMemeAssetCategories = values().map { it.value to it }.toMap()

    fun fromValue(value: Int): MemeAssetCategory =
      mappedMemeAssetCategories[value] ?: MOTIVATION
  }
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
  val assetCategories: Set<MemeAssetCategory>,
  val characters: List<CharacterEntity>, // should already be downloaded at startup
  val representation: VisualAssetRepresentation,
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
) : ContentRepresentation {
  fun toEntity(characters: List<CharacterEntity>): VisualAssetEntity =
    VisualAssetEntity(
      id,
      path,
      alt,
      cat.map { MemeAssetCategory.fromValue(it) }.toSet(),
      characters,
      this
    )
}

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
) : AssetRepresentation {
  fun toEntity(): AnimeEntity =
    AnimeEntity(id, name)
}

data class AnimeEntity(
  val id: String,
  val name: String,
) : Comparable<AnimeEntity> {
  override fun compareTo(other: AnimeEntity): Int =
    name.compareTo(other.name, ignoreCase = true)
}

@Suppress("MagicNumber")
enum class Gender(
  val value: Int,
  private val ordinalValue: Int
) {
  FEMALE(1, 0),
  MALE(2, 1),
  OTHER(4, 2);

  companion object {
    private val mappedGenders = values().map { it.ordinalValue to it }.toMap()

    fun fromValue(ordinalValue: Int): Gender =
      mappedGenders[ordinalValue] ?: OTHER
  }
}

data class CharacterRepresentation(
  override val id: String,
  val animeId: String,
  val name: String,
  val gender: Int,
) : AssetRepresentation {
  fun toEntity(anime: AnimeEntity): CharacterEntity =
    CharacterEntity(
      id,
      anime,
      name,
      Gender.fromValue(gender)
    )
}

data class CharacterEntity(
  val id: String,
  val anime: AnimeEntity,
  val name: String,
  val gender: Gender,
)
