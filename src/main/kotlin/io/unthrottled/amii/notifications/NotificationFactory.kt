package io.unthrottled.amii.notifications

import com.intellij.openapi.project.Project
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.BalloonTools.fetchBalloonParameters
import javax.swing.JLayeredPane
import javax.swing.JPanel

object NotificationFactory {

  @Suppress("MaxLineLength")
  fun dispatchNotification(project: Project) {
    val (ideFrame, notificationPosition) = fetchBalloonParameters(project)
    val label = JBLabel(
      """<html>
<div style='text-align: center'>
  <h4 style='color: ${ColorUtil.toHex(UIUtil.getContextHelpForeground())}'>Caramelldansen</h2>
</div>
<div style='margin: 5px;'>
  <img
    alt='das image'
    src='file:///home/alex/workspace/waifuMotivator/waifu-motivator-plugin/build/idea-sandbox/config/waifuMotivationAssets/visuals/caramelldansen.gif'/>
</div>
</html>
        """
    )
    val rootPane = UIUtil.getRootPane(ideFrame.component)
    val layeredPane = rootPane?.layeredPane
    val panel = JPanel()
    panel.add(label)
    panel.size = label.preferredSize

    layeredPane?.add(panel)
    layeredPane?.setLayer(panel, JLayeredPane.POPUP_LAYER, 0)
  }
}
