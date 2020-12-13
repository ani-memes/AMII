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
}

interface AssetContentDefinition : AssetDefinition {
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
  val char: List<String>,
  val aud: String? = null,
) : AssetContentDefinition {

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
) : AssetContentDefinition {
  fun toContent(assetUrl: URI): AudibleMemeContent =
    AudibleMemeContent(
      assetUrl,
    )
}

data class AudibleMemeContent(
  val filePath: URI
) : Content

data class AnimeAsset(
  override val id: String,
  val name: String,
) : AssetDefinition

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

data class CharacterAsset(
  override val id: String,
  val animeId: String,
  val name: String,
  val gender: Int,
) : AssetDefinition {

  val characterGender: Gender
    get() = Gender.fromValue(gender)
}
