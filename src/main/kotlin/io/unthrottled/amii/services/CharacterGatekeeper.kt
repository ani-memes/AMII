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

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private var preferredCharactersIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.preferredCharacters)
  private var blackListedCharactersIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.blackListedCharacters)
  private var preferredGenders: Int = Config.instance.preferredGenders

  private fun extractAllowedCharactersFromState(characterConfig: String): Set<String> =
    characterConfig.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .map { it.toLowerCase() }
      .toSet()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        preferredCharactersIds = extractAllowedCharactersFromState(Config.instance.preferredCharacters)
        blackListedCharactersIds = extractAllowedCharactersFromState(Config.instance.blackListedCharacters)
        preferredGenders = newPluginState.preferredGenders
      }
    )
  }

  fun hasPreferredCharacter(characters: List<CharacterEntity>): Boolean =
    passesCharacterBlackList(characters) &&
      (
        preferredCharactersIds.isEmpty() ||
          characters.any { isPreferred(it) }
        )

  private fun passesCharacterBlackList(characters: List<CharacterEntity>) =
    (blackListedCharactersIds.isEmpty() || characters.none { isBlackListed(it) })

  fun hasPreferredGender(characters: List<CharacterEntity>): Boolean =
    characters.isEmpty() || characters.any { Config.instance.genderPreferred(it.gender) }

  fun isPreferred(character: CharacterEntity): Boolean =
    preferredCharactersIds.contains(character.id)

  fun isBlackListed(character: CharacterEntity): Boolean =
    blackListedCharactersIds.contains(character.id)

  override fun dispose() {
    connection.dispose()
  }
}
