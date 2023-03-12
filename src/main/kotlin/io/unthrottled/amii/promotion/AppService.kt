package io.unthrottled.amii.promotion

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo

enum class SpecialSnowflakeIDE {
  RIDER, ANDROID_STUDIO, NORMIE
}

interface SpecialSnowflakeService {

  fun getSpecialSnowflakeStatus(): SpecialSnowflakeIDE

  companion object {
    fun instance(): SpecialSnowflakeService? =
      ApplicationManager.getApplication()?.getService(SpecialSnowflakeService::class.java)
  }
}

class NormieService : SpecialSnowflakeService {
  override fun getSpecialSnowflakeStatus(): SpecialSnowflakeIDE =
    SpecialSnowflakeIDE.NORMIE
}
class RiderSnowflakeService : SpecialSnowflakeService {
  override fun getSpecialSnowflakeStatus(): SpecialSnowflakeIDE =
    SpecialSnowflakeIDE.RIDER
}
class AndroidStudioSnowflakeService : SpecialSnowflakeService {
  override fun getSpecialSnowflakeStatus(): SpecialSnowflakeIDE =
    SpecialSnowflakeIDE.ANDROID_STUDIO
}

object AppService {
  fun getApplicationName(): String =
    ApplicationNamesInfo.getInstance().fullProductNameWithEdition

  fun isRiderPlatform(): Boolean =
    SpecialSnowflakeService.instance()?.getSpecialSnowflakeStatus() == SpecialSnowflakeIDE.RIDER

  fun isAndroidStudio(): Boolean =
    SpecialSnowflakeService.instance()?.getSpecialSnowflakeStatus() == SpecialSnowflakeIDE.ANDROID_STUDIO
}
