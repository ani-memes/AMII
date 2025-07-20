package io.unthrottled.amii.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.ui.UIUtil
import javax.swing.JLayeredPane

fun Project.getRootPane(): JLayeredPane? {
  // Try multiple approaches to get the root pane
  
  // Approach 1: Original method via BalloonTools
  val originalResult = UIUtil.getRootPane(
    BalloonTools.getIDEFrame(this).orElse(null)?.component
  )?.layeredPane
  
  if (originalResult != null) {
    return originalResult
  }
  
  // Approach 2: Direct WindowManager approach
  val windowManager = WindowManager.getInstance()
  
  // Try getting frame directly for this project
  var ideFrame = windowManager.getIdeFrame(this)
  if (ideFrame != null) {
    val rootPane = UIUtil.getRootPane(ideFrame.component)?.layeredPane
    if (rootPane != null) return rootPane
  }
  
  // Try getting any project frame
  val allFrames = windowManager.allProjectFrames
  for (frame in allFrames) {
    if (frame.project == this) {
      val rootPane = UIUtil.getRootPane(frame.component)?.layeredPane
      if (rootPane != null) return rootPane
    }
  }
  
  // Try any frame with a project
  for (frame in allFrames) {
    if (frame.project != null) {
      val rootPane = UIUtil.getRootPane(frame.component)?.layeredPane
      if (rootPane != null) return rootPane
    }
  }
  
  // Last resort - try any frame at all
  for (frame in allFrames) {
    val rootPane = UIUtil.getRootPane(frame.component)?.layeredPane
    if (rootPane != null) return rootPane
  }
  
  return null
}
