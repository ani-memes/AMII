package io.unthrottled.amii.tools

import com.intellij.ui.ColorUtil
import java.awt.Color

fun Color.toHexString() = "#${ColorUtil.toHex(this)}"
