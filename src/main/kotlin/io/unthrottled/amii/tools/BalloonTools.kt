package io.unthrottled.amii.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.util.Optional

object BalloonTools {
  private const val NOTIFICATION_Y_OFFSET = 20
  fun fetchBalloonParameters(project: Project): Optional<Pair<IdeFrame, RelativePoint>> =
    getIDEFrame(project)
      .map { ideFrame ->
        val frameBounds = ideFrame.component.bounds
        val notificationPosition = RelativePoint(
          ideFrame.component,
          Point(frameBounds.x, NOTIFICATION_Y_OFFSET)
        )
        Pair(ideFrame, notificationPosition)
      }

  fun getIDEFrame(project: Project) =
    (
      WindowManager.getInstance().getIdeFrame(project)
        ?: WindowManager.getInstance().allProjectFrames.firstOrNull()
        ?: WindowManager.getInstance().allProjectFrames.find { it.project == project }
        ?: WindowManager.getInstance().allProjectFrames.find { it.project != null }
      ).toOptional()
}
