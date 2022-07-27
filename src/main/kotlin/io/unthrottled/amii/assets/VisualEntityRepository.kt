package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.actions.SyncedAssetsListener
import io.unthrottled.amii.assets.LocalVisualContentManager.updateRepresentation
import io.unthrottled.amii.platform.LifeCycleManager
import io.unthrottled.amii.platform.UpdateAssetsListener
import java.util.concurrent.ConcurrentHashMap

class VisualEntityRepository : Disposable {
  companion object {
    val instance: VisualEntityRepository
      get() = ApplicationManager.getApplication().getService(VisualEntityRepository::class.java)

    private const val ANIME_SYNC = 1
    private const val CHARACTER_SYNC = 2
    private const val VISUAL_SYNC = 4
    private const val ALL_SYNCED = ANIME_SYNC or CHARACTER_SYNC or VISUAL_SYNC
  }

  private var syncedAssets = 0

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    LifeCycleManager.registerAssetUpdateListener(
      object : UpdateAssetsListener {
        override fun onRequestedUpdate() {
          syncedAssets = 0
        }

        override fun onRequestedBackgroundUpdate() {
          syncedAssets = 0
        }
      }
    )

    // there is a bit of a circular dependency between
    // the <code>VisualContentManager</code> and this
    // class, so we'll just register the update listener
    // after all the services initialize.
    ApplicationManager.getApplication().invokeLater {
      messageBusConnection
        .subscribe(
          ContentManagerListener.TOPIC,
          ContentManagerListener {
            syncedAssets = syncedAssets or when (it) {
              AssetCategory.ANIME -> ANIME_SYNC
              AssetCategory.CHARACTERS -> CHARACTER_SYNC
              AssetCategory.VISUALS -> VISUAL_SYNC
              else -> syncedAssets
            }
            if (syncedAssets == ALL_SYNCED) {
              updateIndices()
              syncedAssets = 0
              ApplicationManager.getApplication().messageBus.syncPublisher(SyncedAssetsListener.TOPIC)
                .onSynced()
            }
          }
        )
    }
  }

  private var visualAssetEntities: Map<String, VisualAssetEntity>
  private var localVisualAssetEntities: MutableMap<String, VisualAssetEntity>
  private var allAnime: Map<String, AnimeEntity>
  private var characters: Map<String, CharacterEntity>

  init {
    allAnime = createAnimeIndex()
    characters = createCharacterIndex()
    visualAssetEntities = createVisualAssetIndex()
    localVisualAssetEntities = createLocalVisualIndex()
  }

  private fun updateIndices() {
    allAnime = createAnimeIndex()
    characters = createCharacterIndex()
    visualAssetEntities = createVisualAssetIndex()
    refreshLocalAssets()
  }

  fun refreshLocalAssets() {
    localVisualAssetEntities = createLocalVisualIndex()
  }

  val allCharacters: List<CharacterEntity>
    get() = characters.entries.map { it.value }

  fun findById(assetId: String): VisualAssetEntity? {
    return visualAssetEntities[assetId] ?: localVisualAssetEntities[assetId]
  }

  fun update(visualAssetEntity: VisualAssetEntity) {
    updateRepresentation(
      visualAssetEntity.representation
    )
    localVisualAssetEntities[visualAssetEntity.id] = visualAssetEntity
  }

  private fun createAnimeIndex() =
    AnimeContentManager.supplyAssets()
      .associate { it.id to it.toEntity() }

  private fun createCharacterIndex() = CharacterContentManager.supplyAssets()
    .filter { allAnime.containsKey(it.animeId) }
    .associate { it.id to it.toEntity(allAnime[it.animeId]!!) }

  private fun createVisualAssetIndex(): ConcurrentHashMap<String, VisualAssetEntity> {
    return ConcurrentHashMap(
      RemoteVisualContentManager.supplyAllAssetDefinitions()
        .map { visualRepresentation ->
          visualRepresentation.toEntity(visualRepresentation.char.mapNotNull { characters[it] })
        }.associateBy { it.id }
    )
  }

  private fun createLocalVisualIndex(): ConcurrentHashMap<String, VisualAssetEntity> {
    return ConcurrentHashMap(
      LocalVisualContentManager.supplyAllUserModifiedVisualRepresentations()
        .map { visualRepresentation ->
          visualRepresentation.fromCustomEntity()
        }.associateBy { it.id }
    )
  }

  override fun dispose() {
    messageBusConnection.dispose()
  }
}
