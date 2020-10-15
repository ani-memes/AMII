package io.unthrottled.amii.services

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.tools.MyBundle

class MyApplicationService {

  companion object {
    val instance: MyApplicationService
      get() = ApplicationManager.getApplication().getService(MyApplicationService::class.java)
  }

  init {
    println(getMessage())
  }

  fun getMessage() = MyBundle.message("applicationService")
}
