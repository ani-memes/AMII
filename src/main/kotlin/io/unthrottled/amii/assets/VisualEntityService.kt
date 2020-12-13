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
      characterEntities = createIndex()
    }
  }

  private var characterEntities: ConcurrentHashMap<String, VisualAssetEntity>

  init {
    characterEntities = createIndex()
  }

  private fun createIndex(): ConcurrentHashMap<String, VisualAssetEntity> {
    val allAnime = AnimeContentManager.supplyAssets().map { it.id to it.toEntity() }.toMap()
    val characters = CharacterContentManager.supplyAssets()
      .filter { allAnime.containsKey(it.animeId) }
      .map { it.id to it.toEntity(allAnime[it.animeId]!!) }.toMap()
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
      .mapNotNull { characterEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) }

  fun supplyPreferredGenderLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { characterEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  fun supplyPreferredRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { characterEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) }

  fun supplyAllRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { characterEntities[it.id] }

  fun supplyAllLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { characterEntities[it.id] }

  fun supplyPreferredGenderRemoteAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { characterEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  override fun dispose() {}
}
