package io.unthrottled.amii.tools

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil

fun Project.getRootPane() = UIUtil.getRootPane(
  BalloonTools.getIDEFrame(this).component
)?.layeredPane
