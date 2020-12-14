package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.services.CharacterGatekeeper
import java.util.concurrent.ConcurrentHashMap

class VisualEntityService : Disposable {

  companion object {
    val instance: VisualEntityService
      get() = ApplicationManager.getApplication().getService(VisualEntityService::class.java)
  }

  init {
    LifeCycleManager.registerAssetUpdateListener {
      allAnime = createAnimeIndex()
      characters = createCharacterIndex()
      visualAssetEntities = createIndex()
    }
  }

  private var visualAssetEntities: ConcurrentHashMap<String, VisualAssetEntity>
  private var allAnime: Map<String, AnimeEntity>
  private var characters: Map<String, CharacterEntity>

  init {
    allAnime = createAnimeIndex()
    characters = createCharacterIndex()
    visualAssetEntities = createIndex()
  }

  val allCharacters: List<CharacterEntity>
    get() = characters.entries.map { it.value }

  private fun createAnimeIndex() = AnimeContentManager.supplyAssets().map { it.id to it.toEntity() }.toMap()
  private fun createCharacterIndex() = CharacterContentManager.supplyAssets()
    .filter { allAnime.containsKey(it.animeId) }
    .map { it.id to it.toEntity(allAnime[it.animeId]!!) }.toMap()

  private fun createIndex(): ConcurrentHashMap<String, VisualAssetEntity> {
    return ConcurrentHashMap(
      VisualContentManager.supplyAllAssetDefinitions()
        .map { visualRepresentation ->
          visualRepresentation.toEntity(visualRepresentation.char.mapNotNull { characters[it] })
        }.map { it.id to it }
        .toMap()
    )
  }

  fun supplyPreferredLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) }

  fun supplyPreferredGenderLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  fun supplyPreferredRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) }

  fun supplyAllRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }

  fun supplyAllLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }

  fun supplyPreferredGenderRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  override fun dispose() {}
}
