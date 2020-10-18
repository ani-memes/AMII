package io.unthrottled.amii.memes

import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.HwFacadeJPanel
import com.intellij.util.Alarm
import com.intellij.util.ui.Animator
import com.intellij.util.ui.JBUI
import java.awt.AlphaComposite
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JLayeredPane

// todo: positioning, option to hide mouse click
class MemePanel(
  private val rootPane: JLayeredPane,
  private val meme: String
) : HwFacadeJPanel(), Disposable {

  companion object {
    private const val TOTAL_FRAMES = 10
    private const val CYCLE_DURATION = 250
    private const val MEME_DISPLAY_LIFETIME = 3000
    private const val PANEL_PADDING = 10
    private const val NOTIFICATION_Y_OFFSET = 20
  }

  private var alpha = 0.0f

  private val fadeoutAlarm = Alarm(this)

  init {
    val label = JBLabel(meme)

    this.add(label)
    val memeSize = label.preferredSize
    val width = memeSize.width + PANEL_PADDING
    this.size = Dimension(width, memeSize.height + PANEL_PADDING)
    this.border = JBUI.Borders.customLine(
      JBColor.namedColor(
        "Notification.borderColor",
        NotificationsManagerImpl.BORDER_COLOR
      )
    )

    setLocation(rootPane.x + rootPane.width - width, NOTIFICATION_Y_OFFSET)
  }

  fun display() {
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
          Disposer.dispose(self)
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
