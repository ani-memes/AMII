package io.unthrottled.amii.promotion

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.util.PlatformUtils

object AppService {
  fun getApplicationName(): String =
    ApplicationNamesInfo.getInstance().fullProductNameWithEdition

  fun isRiderPlatform(): Boolean = PlatformUtils.isRider()
}
