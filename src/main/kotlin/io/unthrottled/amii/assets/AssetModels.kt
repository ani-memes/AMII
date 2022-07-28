package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.PluginMessageBundle
import java.net.URI

@Suppress("MagicNumber")
enum class MemeAssetCategory(
  val value: Int,
  val description: String,
) {
  ACKNOWLEDGEMENT(0, PluginMessageBundle.message("meme.asset.category.acknowledgement")),
  FRUSTRATION(1, PluginMessageBundle.message("meme.asset.category.frustration")),
  ENRAGED(2, PluginMessageBundle.message("meme.asset.category.enraged")),
  CELEBRATION(3, PluginMessageBundle.message("meme.asset.category.celebration")),
  HAPPY(4, PluginMessageBundle.message("meme.asset.category.happy")),
  SMUG(5, PluginMessageBundle.message("meme.asset.category.smug")),
  WAITING(6, PluginMessageBundle.message("meme.asset.category.waiting")),
  MOTIVATION(7, PluginMessageBundle.message("meme.asset.category.motivation")),
  WELCOMING(8, PluginMessageBundle.message("meme.asset.category.welcoming")),
  DEPARTURE(9, PluginMessageBundle.message("meme.asset.category.departure")),
  ENCOURAGEMENT(10, PluginMessageBundle.message("meme.asset.category.encouragement")),
  MOCKING(11, PluginMessageBundle.message("meme.asset.category.mocking")),
  SHOCKED(12, PluginMessageBundle.message("meme.asset.category.shocked")),
  DISAPPOINTMENT(13, PluginMessageBundle.message("meme.asset.category.disappointment")),
  ALERT(14, PluginMessageBundle.message("meme.asset.category.alert")),
  BORED(15, PluginMessageBundle.message("meme.asset.category.bored")),
  TIRED(16, PluginMessageBundle.message("meme.asset.category.tired")),
  PATIENTLY_WAITING(17, PluginMessageBundle.message("meme.asset.category.patiently_waiting")),
  POUTING(18, PluginMessageBundle.message("meme.asset.category.pouting")),
  ;

  companion object {
    private val mappedMemeAssetCategories = values().associateBy { it.value }

    fun fromValue(value: Int): MemeAssetCategory =
      mappedMemeAssetCategories[value] ?: MOTIVATION

    fun sortedValues() = values().sortedBy(MemeAssetCategory::name)
  }
}

interface Content

// representation == REST API model
interface AssetRepresentation {
  val id: String
  val del: Boolean?
}

// content == has something to download
interface ContentRepresentation : AssetRepresentation {
  val path: String
}

data class VisualMemeContent(
  val id: String,
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
  val isCustomAsset: Boolean = false,
) {
  val isLewd: Boolean
    get() = representation.lewd == true
  fun toContent(assetUrl: URI): VisualMemeContent =
    VisualMemeContent(
      id,
      assetUrl,
      alt,
      audibleAssetId,
    )

  fun duplicate(categories: Set<MemeAssetCategory>, audibleAssetId: String?, representation: VisualAssetRepresentation) =
    copy(assetCategories = categories, audibleAssetId = audibleAssetId, representation = representation)
}

data class VisualAssetRepresentation(
  override val id: String,
  override val path: String,
  val alt: String,
  val cat: MutableList<Int>,
  val char: List<String>,
  val aud: String? = null,
  val lewd: Boolean? = false,
  override val del: Boolean? = null,
) : ContentRepresentation {
  fun toEntity(characters: List<CharacterEntity>): VisualAssetEntity =
    visualAssetEntity(characters, false)

  fun fromCustomEntity(): VisualAssetEntity =
    visualAssetEntity(emptyList(), true)

  fun duplicate(newCategories: MutableList<Int>, audibleAssetId: String?) =
    copy(cat = newCategories, aud = audibleAssetId)

  fun culturedDuplicate(cultured: Boolean) =
    copy(lewd = cultured)

  fun duplicateWithNewPath(path: String) =
    copy(path = path)

  private fun visualAssetEntity(
    characters: List<CharacterEntity>,
    isCustomAsset: Boolean,
  ) =
    VisualAssetEntity(
      id,
      path,
      alt,
      cat.map { MemeAssetCategory.fromValue(it) }.toSet(),
      characters,
      audibleAssetId = aud,
      isCustomAsset = isCustomAsset,
      representation = this
    )
}

data class AudibleRepresentation(
  override val id: String,
  override val path: String,
  override val del: Boolean? = null,
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
  override val del: Boolean? = null,
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
  FEMALE(1 shl 0, 0),
  MALE(1 shl 1, 1),
  OTHER(1 shl 2, 2);

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
  override val del: Boolean? = null,
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
