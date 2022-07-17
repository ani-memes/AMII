package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.services.CharacterGatekeeper

class VisualEntityService : Disposable {

  companion object {
    val instance: VisualEntityService
      get() = ApplicationManager.getApplication().getService(VisualEntityService::class.java)
  }

  fun supplyPreferredLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }
      .filter {
        CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) &&
          CharacterGatekeeper.instance.hasPreferredGender(it.characters)
      }

  fun supplyPreferredGenderLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  fun supplyPreferredRemoteAssetDefinitions(): List<VisualAssetEntity> =
    RemoteVisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }
      .filter {
        CharacterGatekeeper.instance.hasPreferredCharacter(it.characters) &&
          CharacterGatekeeper.instance.hasPreferredGender(it.characters)
      }

  fun supplyAllRemoteAssetDefinitions(): List<VisualAssetEntity> =
    RemoteVisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }

  fun supplyAllLocalAssetDefinitions(): List<VisualAssetEntity> =
    VisualContentManager.supplyAllLocalAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }

  fun supplyPreferredGenderRemoteAssetDefinitions(): List<VisualAssetEntity> =
    RemoteVisualContentManager.supplyAllRemoteAssetDefinitions()
      .mapNotNull { VisualEntityRepository.instance.visualAssetEntities[it.id] }
      .filter { CharacterGatekeeper.instance.hasPreferredGender(it.characters) }

  override fun dispose() {}
}
