package io.unthrottled.amii.tools

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
const val DEFAULT_MESSAGE_BUNDLE = "messages.AMII"

object PluginMessageBundle : AbstractBundle(DEFAULT_MESSAGE_BUNDLE) {

  @JvmStatic
  fun message(
    @PropertyKey(resourceBundle = DEFAULT_MESSAGE_BUNDLE) key: String,
    vararg params: Any
  ) =
    getMessage(key, *params)

  fun messagePointer(
    @PropertyKey(resourceBundle = DEFAULT_MESSAGE_BUNDLE) key: String,
    vararg params: Any
  ) = run {
    message(key, *params)
  }
}
