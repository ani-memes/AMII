package io.unthrottled.amii.memes

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.assets.VisualAssetDefinitionService
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.tools.BalloonTools.getIDEFrame
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object MemeFactory {

  fun createMemeDisplay(project: Project): Optional<MemePanel> =
    VisualAssetDefinitionService
      .getRandomAssetByCategory(MemeAssetCategory.MOTIVATION)
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
      }
}
