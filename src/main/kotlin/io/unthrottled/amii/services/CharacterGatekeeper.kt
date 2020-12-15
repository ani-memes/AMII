package io.unthrottled.amii.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.amii.assets.CharacterEntity
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ConfigListener.Companion.CONFIG_TOPIC

class CharacterGatekeeper : Disposable {
  companion object {
    val instance: CharacterGatekeeper
      get() = ServiceManager.getService(CharacterGatekeeper::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect(this)

  private var preferredCharactersIds: Set<String> = extractAllowedCharactersFromState(Config.instance)
  private var preferredGenders: Int = Config.instance.preferredGenders

  private fun extractAllowedCharactersFromState(pluginConfig: Config): Set<String> =
    pluginConfig.preferredCharacters.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .map { it.toLowerCase() }
      .toSet()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        preferredCharactersIds = extractAllowedCharactersFromState(newPluginState)
        preferredGenders = newPluginState.preferredGenders
      }
    )
  }

  fun hasPreferredCharacter(characters: List<CharacterEntity>?): Boolean =
    (preferredCharactersIds.isEmpty() && hasPreferredGender(characters)) ||
      characters?.any { preferredCharactersIds.contains(it.id) } ?: false

  fun hasPreferredGender(characters: List<CharacterEntity>?): Boolean =
    preferredGenders == 0 || characters?.any {
      val characterGender = it.gender.value
      (characterGender and preferredGenders) == characterGender
    } ?: false

  fun isPreferred(character: CharacterEntity): Boolean =
    preferredCharactersIds.contains(character.id)

  override fun dispose() {}
}
