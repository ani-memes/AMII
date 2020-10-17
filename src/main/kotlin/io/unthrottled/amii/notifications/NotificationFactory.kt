package io.unthrottled.amii.notifications

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.HwFacadeJPanel
import com.intellij.util.ui.Animator
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.BalloonTools.fetchBalloonParameters
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JLayeredPane

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
    val panel = MemePanel()
    panel.add(label)
    panel.size = label.preferredSize

    layeredPane?.add(panel)
    layeredPane?.setLayer(panel, JLayeredPane.POPUP_LAYER, 0)

    runAnimation(layeredPane, panel)
  }

  const val TOTAL_FRAMES = 8
  const val CYCLE_DURATION = 500

  private fun runAnimation(layeredPane: JLayeredPane?, memePanel: MemePanel) {
    val animator = object : Animator("Meme Machine", TOTAL_FRAMES, CYCLE_DURATION, false) {
      override fun paintNow(frame: Int, totalFrames: Int, cycle: Int) {
        memePanel.alpha = frame.toFloat() / totalFrames
      }

      override fun paintCycleEnd() {
        if (isForward) {
          memePanel.repaint()

          // set fade out timeer
        } else {
          layeredPane?.remove(memePanel)
          layeredPane?.revalidate()
          layeredPane?.revalidate()
        }
        Disposer.dispose(this)
      }
    }

    animator.resume()
  }
}

class MemePanel : HwFacadeJPanel(), Disposable {

  var alpha = 0.0f

  override fun paintComponent(g: Graphics?) {
    super.paintComponent(g)
    if (g !is Graphics2D) return

    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
  }

  override fun dispose() {
  }
}
