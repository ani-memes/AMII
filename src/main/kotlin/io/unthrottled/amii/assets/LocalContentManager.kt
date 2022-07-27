package io.unthrottled.amii.assets

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.tools.PluginMessageBundle

object LocalContentManager {

  @JvmStatic
  fun refreshStuff(project: Project?) {
    ApplicationManager.getApplication().executeOnPooledThread {
      // order here is important because the
      // repo has a dependency on the local visual
      // content manager.
      LocalVisualContentManager.rescanDirectory()
      VisualEntityRepository.instance.refreshLocalAssets()

      UpdateNotification.sendMessage(
        PluginMessageBundle.message("amii.local.asset.sync.done.title"),
        PluginMessageBundle.message("amii.local.asset.sync.done.body"),
        project
      )
    }
  }
}
