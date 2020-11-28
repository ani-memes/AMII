package io.unthrottled.amii.memes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.assets.VisualAssetDefinitionService
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.BalloonTools.getIDEFrame
import io.unthrottled.amii.tools.doOrElse
import io.unthrottled.amii.tools.toOptional

class Meme(
  private val memePanel: MemePanel
) {

  fun display() {
    ApplicationManager.getApplication().invokeLater {
      memePanel.display()
    }
  }
}

object MemeFactory {

  fun createMeme(
    project: Project,
    memeAssetCategory: MemeAssetCategory,
    memeCallback: (Meme) -> Unit
  ) {
    ExecutionService.executeAsynchronously {
      VisualAssetDefinitionService
        .getRandomAssetByCategory(memeAssetCategory)
        .flatMap { visualMeme ->
          UIUtil.getRootPane(
            getIDEFrame(project).component
          )?.layeredPane
            .toOptional()
            .map {
              MemePanel(
                it,
                visualMeme,
                MemePanelSettings(
                  Config.instance.notificationMode,
                  Config.instance.notificationAnchor,
                  Config.instance.memeDisplayInvulnerabilityDuration,
                  Config.instance.memeDisplayTimedDuration
                )
              )
            }
        }.doOrElse({
          memeCallback(Meme(it))
        }) {
        }
    }
  }
}
