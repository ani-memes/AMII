package io.unthrottled.amii.platform

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager

object LifeCycleManager : Disposable {

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  fun registerAssetUpdateListener(updateAssetsListener: UpdateAssetsListener) {
    connection.subscribe(UpdateAssetsListener.TOPIC, updateAssetsListener)
  }

  override fun dispose() {
    connection.dispose()
  }
}
