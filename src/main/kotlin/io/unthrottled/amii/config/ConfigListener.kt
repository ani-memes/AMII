package io.unthrottled.amii.config

import com.intellij.util.messages.Topic
import java.util.EventListener

val CONFIG_TOPIC: Topic<ConfigListener> =
  Topic(ConfigListener::class.java)

fun interface ConfigListener : EventListener {
  fun themeConfigUpdated(config: Config)
}
