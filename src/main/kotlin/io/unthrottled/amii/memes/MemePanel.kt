package io.unthrottled.amii.memes

import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.HwFacadeJPanel
import com.intellij.util.Alarm
import com.intellij.util.ui.Animator
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.config.ui.NotificationAnchor.BOTTOM_CENTER
import io.unthrottled.amii.config.ui.NotificationAnchor.BOTTOM_LEFT
import io.unthrottled.amii.config.ui.NotificationAnchor.BOTTOM_RIGHT
import io.unthrottled.amii.config.ui.NotificationAnchor.CENTER
import io.unthrottled.amii.config.ui.NotificationAnchor.MIDDLE_LEFT
import io.unthrottled.amii.config.ui.NotificationAnchor.TOP_CENTER
import io.unthrottled.amii.config.ui.NotificationAnchor.TOP_LEFT
import io.unthrottled.amii.config.ui.NotificationAnchor.TOP_RIGHT
import java.awt.AWTEvent.KEY_EVENT_MASK
import java.awt.AWTEvent.MOUSE_EVENT_MASK
import java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK
import java.awt.AlphaComposite
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JLayeredPane
import javax.swing.MenuElement
import javax.swing.SwingUtilities

// todo: option to hide mouse click, Invulnerable to clicks before animation shows.
class MemePanel(
  private val rootPane: JLayeredPane,
  meme: String,
  config: Config
) : HwFacadeJPanel(), Disposable {

  companion object {
    private const val TOTAL_FRAMES = 8
    private const val CYCLE_DURATION = 500
    private const val MEME_DISPLAY_LIFETIME = 3000
    private const val PANEL_PADDING = 10
    private const val NOTIFICATION_Y_OFFSET = 10
    private const val HALF_DIVISOR = 2
    private const val fadeoutDelay = 100
    private val log = Logger.getInstance(MemePanel::class.java)
  }

  private var alpha = 0.0f

  private val fadeoutAlarm = Alarm(this)
  private val mouseListener: AWTEventListener

  init {
    val memeDisplay = createMemeDisplay(meme)
    val (width, height) = initializeSize(memeDisplay)
    positionPanel(config, width, height)
    mouseListener = AWTEventListener { e ->
      if (e is MouseEvent && e.id == MouseEvent.MOUSE_PRESSED && !isInsideMemePanel(e)) {
        log.warn("Finna Hide on click outside")
        fadeoutAlarm.cancelAllRequests()
        fadeoutAlarm.addRequest({ runAnimation(false) }, fadeoutDelay)
      } else if (e is KeyEvent && e.id == KeyEvent.KEY_PRESSED) {
        when (e.keyCode) {
          KeyEvent.VK_ESCAPE -> {
            log.warn("Finna Hide on escape")
            fadeoutAlarm.cancelAllRequests()
            fadeoutAlarm.addRequest({ runAnimation(false) }, fadeoutDelay)
          }
        }
      }
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(
      mouseListener,
      MOUSE_EVENT_MASK or MOUSE_MOTION_EVENT_MASK or KEY_EVENT_MASK
    )
  }

  private fun isInsideMemePanel(e: MouseEvent): Boolean {
    val target = RelativePoint(e)
    val cmp = target.originalComponent
    return when {
      cmp.isShowing.not() -> true
      cmp is MenuElement -> false
      UIUtil.isDescendingFrom(cmp, this) -> true
      this.isShowing.not() -> false
      else -> {
        val point = target.screenPoint
        SwingUtilities.convertPointFromScreen(point, this)
        this.contains(point)
      }
    }
  }

  private fun createMemeDisplay(meme: String): JBLabel {
    val memeDisplay = JBLabel(meme)
    this.add(memeDisplay)
    return memeDisplay
  }

  private fun initializeSize(memeDisplay: JBLabel): Pair<Int, Int> {
    val memeSize = memeDisplay.preferredSize
    val width = memeSize.width + PANEL_PADDING
    val height = memeSize.height + PANEL_PADDING
    this.size = Dimension(width, height)
    this.border = JBUI.Borders.customLine(
      JBColor.namedColor(
        "Notification.borderColor",
        NotificationsManagerImpl.BORDER_COLOR
      )
    )
    return width to height
  }

  private fun positionPanel(config: Config, width: Int, height: Int) {
    val (x, y) = getPosition(
      NotificationAnchor.fromValue(config.notificationAnchor),
      rootPane.x + rootPane.width,
      rootPane.y + rootPane.height,
      Rectangle(width, height)
    )
    setLocation(x, y)
  }

  private fun getPosition(
    anchor: NotificationAnchor,
    parentWidth: Int,
    parentHeight: Int,
    memePanelBoundingBox: Rectangle,
  ): Pair<Int, Int> {
    return when (anchor) {
      TOP_CENTER, CENTER, BOTTOM_CENTER ->
        (parentWidth - memePanelBoundingBox.width) / HALF_DIVISOR to when (anchor) {
          TOP_CENTER -> NOTIFICATION_Y_OFFSET
          BOTTOM_CENTER -> parentHeight - memePanelBoundingBox.height - NOTIFICATION_Y_OFFSET
          else -> (parentHeight - memePanelBoundingBox.height) / HALF_DIVISOR
        }
      else ->
        when (anchor) {
          TOP_LEFT,
          MIDDLE_LEFT,
          BOTTOM_LEFT -> NOTIFICATION_Y_OFFSET
          else -> parentWidth - memePanelBoundingBox.width
        } to when (anchor) {
          TOP_LEFT, TOP_RIGHT -> NOTIFICATION_Y_OFFSET
          BOTTOM_LEFT, BOTTOM_RIGHT ->
            parentHeight - memePanelBoundingBox.height - NOTIFICATION_Y_OFFSET
          else -> (parentHeight - memePanelBoundingBox.height) / HALF_DIVISOR
        }
    }
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
    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener)
  }
}
