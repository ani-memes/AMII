package io.unthrottled.amii.notifications

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.HwFacadeJPanel
import com.intellij.util.Alarm
import com.intellij.util.ui.Animator
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.BalloonTools.fetchBalloonParameters
import io.unthrottled.amii.tools.toOptional
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JLayeredPane

object NotificationFactory {

  @Suppress("MaxLineLength")
  fun dispatchNotification(project: Project) {
    val (ideFrame, notificationPosition) = fetchBalloonParameters(project)
    val rootPane = UIUtil.getRootPane(ideFrame.component)
    rootPane?.layeredPane
      .toOptional()
      .ifPresent {
        MemePanel(
          it,
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
      }
  }
}

class MemePanel(
  private val rootPane: JLayeredPane,
  private val meme: String
) : HwFacadeJPanel(), Disposable {

  companion object {
    private const val TOTAL_FRAMES = 8
    private const val CYCLE_DURATION = 500
    const val MEME_DISPLAY_LIFETIME = 1000
  }

  var alpha = 0.0f
  private val fadeoutAlarm = Alarm(this)

  init {
    val label = JBLabel(meme)

    this.add(label)
    this.size = label.preferredSize

    rootPane.add(this)

    runAnimation()
  }

  override fun paintComponent(g: Graphics?) {
    super.paintComponent(g)
    if (g !is Graphics2D) return

    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
  }

  private fun runAnimation(runForwards: Boolean = true) {
    val self = this
    val animator = object : Animator(
      "Meme Machine",
      TOTAL_FRAMES,
      CYCLE_DURATION,
      false,
      runForwards
    ) {
      override fun paintNow(frame: Int, totalFrames: Int, cycle: Int) {
        alpha = frame.toFloat() / totalFrames
      }

      override fun paintCycleEnd() {
        if (isForward) {
          self.repaint()
          setFadeOutTimer()
        } else {
          rootPane.remove(self)
          rootPane.revalidate()
          rootPane.repaint()
        }
        Disposer.dispose(this)
      }

      private fun setFadeOutTimer() {
        self.fadeoutAlarm.addRequest(
          { self.runAnimation(false) },
          MEME_DISPLAY_LIFETIME,
          null
        )
      }
    }

    animator.resume()
  }

  override fun dispose() {
  }
}
