package io.unthrottled.amii.tools

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.AMII"

object PluginMessageBundle : AbstractBundle(BUNDLE) {

  @JvmStatic
  fun message(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any,
  ) =
    getMessage(key, *params)

  fun messagePointer(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any,
  ) = run {
    message(key, *params)
  }
}
