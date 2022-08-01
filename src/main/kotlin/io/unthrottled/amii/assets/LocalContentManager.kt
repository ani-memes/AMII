package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationManager

object LocalContentManager {

  @JvmStatic
  fun refreshStuff(onComplete: () -> Unit) {
    ApplicationManager.getApplication().executeOnPooledThread {
      // order here is important because the
      // repo has a dependency on the local visual
      // content manager.
      LocalVisualContentManager.rescanDirectory()
      VisualEntityRepository.instance.refreshLocalAssets()
      onComplete()
    }
  }
}
