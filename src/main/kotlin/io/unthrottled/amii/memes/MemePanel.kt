package io.unthrottled.amii.memes

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.JreHiDpiUtil
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBLayeredPane
import com.intellij.ui.jcef.HwFacadeJPanel
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.Alarm
import com.intellij.util.ui.Animator
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.StartupUiUtil
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.assets.VisualMemeContent
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.config.ui.NotificationAnchor.*
import io.unthrottled.amii.memes.DimensionCappingService.getCappingStyle
import io.unthrottled.amii.memes.PanelDismissalOptions.FOCUS_LOSS
import io.unthrottled.amii.memes.PanelDismissalOptions.TIMED
import io.unthrottled.amii.memes.player.MemePlayer
import io.unthrottled.amii.services.GifService
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.registerDelayedRequest
import io.unthrottled.amii.tools.runSafelyWithResult
import org.intellij.lang.annotations.Language
import java.awt.AWTEvent.*
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.awt.image.RGBImageFilter
import javax.swing.JComponent
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.MenuElement
import javax.swing.SwingUtilities
import kotlin.math.max

enum class PanelDismissalOptions {
  FOCUS_LOSS, TIMED;

  companion object {
    fun fromValue(value: String): PanelDismissalOptions =
      runSafelyWithResult({
        valueOf(value)
      }) { TIMED }
  }
}

data class MemePanelSettings(
  val dismissal: PanelDismissalOptions,
  val anchor: NotificationAnchor,
  val invulnerabilityDuration: Int,
  val displayDuration: Int,
)

@Suppress("TooManyFunctions")
class MemePanel(
  private val rootPane: JLayeredPane,
  val visualMeme: VisualMemeContent,
  private val memePlayer: MemePlayer?,
  private val memePanelSettings: MemePanelSettings,
) : HwFacadeJPanel(), Disposable, Logging {

  companion object {
    val PANEL_LAYER: Int = JBLayeredPane.DRAG_LAYER
    private const val TOTAL_FRAMES = 8
    private const val CYCLE_DURATION = 250
    private const val NOTIFICATION_Y_OFFSET = 10
    private const val HALF_DIVISOR = 2
    private const val fadeoutDelay = 100
    private const val CLEARED_ALPHA = -1f
    private const val WHITE_HEX = 0x00FFFFFF
    private const val TENTH_OF_A_SECOND_MULTIPLICAND = 100

    private val ALLOWED_KEYS = setOf(
      KeyEvent.VK_SHIFT,
      KeyEvent.VK_CONTROL,
      KeyEvent.VK_ALT,
      KeyEvent.VK_META,
    )
  }

  private var alpha = 0.0f
  private var overlay: BufferedImage? = null

  private var invulnerable = memePanelSettings.invulnerabilityDuration > 0

  private val fadeoutAlarm = Alarm()
  private val invulnerabilityAlarm = Alarm()
  private val mouseListener: AWTEventListener = createMouseLister()
  private val memeDisplay: JComponent

  init {
    isOpaque = false
    layout = null

    val (memeContent, memeDisplay) = createMemeContentPanel()
    this.memeDisplay = memeDisplay
    add(memeContent)
    this.size = memeContent.size

    positionMemePanel(
      memePanelSettings,
      memeContent.size.width,
      memeContent.size.height,
    )

    Toolkit.getDefaultToolkit().addAWTEventListener(
      mouseListener,
      MOUSE_EVENT_MASK or MOUSE_MOTION_EVENT_MASK or KEY_EVENT_MASK
    )

    val self = this
    addMouseListener(
      object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {}

        override fun mousePressed(e: MouseEvent?) {}

        override fun mouseReleased(e: MouseEvent?) {}

        override fun mouseEntered(e: MouseEvent?) {
          self.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

        override fun mouseExited(e: MouseEvent?) {
          self.cursor = Cursor.getDefaultCursor()
        }
      }
    )
  }

  private fun createMouseLister(): AWTEventListener {
    var clickedInside = false
    return AWTEventListener { event ->
      if (invulnerable) return@AWTEventListener

      if (
        event !is InputEvent ||
        UIUtil.isDescendingFrom(event.component, rootPane).not()
      ) return@AWTEventListener

      val isFocusLoss = memePanelSettings.dismissal == FOCUS_LOSS
      if (event is MouseEvent) {
        val wasInside = isInsideMemePanel(event)
        if (event.id == MouseEvent.MOUSE_PRESSED) {
          val wasClickedOutsideMeme = !wasInside && isFocusLoss
          if (wasClickedOutsideMeme || clickedInside) {
            dismissMeme()
          } else if (wasInside) {
            fadeoutAlarm.cancelAllRequests()
            clickedInside = true
          }
        }
      } else if (
        event is KeyEvent &&
        event.id == KeyEvent.KEY_PRESSED
      ) {
        if (
          isFocusLoss &&
          ALLOWED_KEYS.contains(event.keyCode).not()
        ) {
          dismissMeme()
        }
      }
    }
  }

  private fun dismissMeme() {
    lifecycleListener.onDismiss()
    fadeoutAlarm.cancelAllRequests()
    registerDelayedRequest(fadeoutAlarm, fadeoutDelay) { runAnimation(false) }
  }

  private var lifecycleListener: MemeLifecycleListener = DEFAULT_MEME_LISTENER
  fun display(dismissalCallback: MemeLifecycleListener) {
    this.lifecycleListener = dismissalCallback
    rootPane.add(this)
    rootPane.setLayer(this, PANEL_LAYER, 0)
    doDumbStuff()
    val invulnerabilityDuration = memePanelSettings.invulnerabilityDuration
    if (invulnerabilityDuration > 0) {
      registerDelayedRequest(
        invulnerabilityAlarm,
        invulnerabilityDuration * TENTH_OF_A_SECOND_MULTIPLICAND
      ) {
        invulnerable = false
      }
    }
    runAnimation()
  }

  /**
   * Fixes: https://github.com/ani-memes/AMII/issues/44
   *
   * I'm not going to pretend like I know what I am doing.
   * I do know that the render issue goes away, when another
   * component is added to the root pane. Finna treat the symptom
   * and not fix the cause.
   */
  private fun doDumbStuff() {
    if (SystemInfo.isMac) {
      val ghostHax = JPanel()
      rootPane.add(ghostHax)
      rootPane.setLayer(ghostHax, PANEL_LAYER)
      rootPane.revalidate()
      rootPane.repaint()
      rootPane.remove(ghostHax)
      rootPane.revalidate()
      rootPane.repaint()
    }
  }

  fun dismiss() {
    removeMeme()
  }

  private fun isInsideMemePanel(e: MouseEvent): Boolean =
    isInsideComponent(e, this)

  private fun isInsideComponent(e: MouseEvent, rootPane1: JComponent): Boolean {
    val target = RelativePoint(e)
    val ogComponent = target.originalComponent
    return when {
      ogComponent.isShowing.not() -> true
      ogComponent is MenuElement -> false
      UIUtil.isDescendingFrom(ogComponent, rootPane1) -> true
      this.isShowing.not() -> false
      else -> {
        val point = target.screenPoint
        SwingUtilities.convertPointFromScreen(point, rootPane1)
        rootPane1.contains(point)
      }
    }
  }

  // todo: post-mvp: shadow
  private fun createMemeContentPanel(): Pair<JComponent, JComponent> {
    val memeContent = JPanel()
    memeContent.layout = null
    val extraStyles = getExtraStyles()

    @Language("HTML")
    val stickerHTML = """<html>
           <img src='${visualMeme.filePath}'
                alt='${visualMeme.imageAlt}'
                $extraStyles />
         </html>
      """
    val memeDisplay = JBLabel(
      stickerHTML
    )
    val memeSize = memeDisplay.preferredSize
    memeContent.size = Dimension(
      memeSize.width,
      memeSize.height,
    )
    memeContent.isOpaque = false
    memeContent.add(memeDisplay)
    val parentInsets = memeDisplay.insets
    memeDisplay.setBounds(
      parentInsets.left,
      parentInsets.top,
      memeSize.width,
      memeSize.height
    )

    return memeContent to memeDisplay
  }

  private fun getExtraStyles(): String =
    if (Config.instance.capDimensions) {
      getCappedDimensions()
    } else {
      ""
    }

  private fun getCappedDimensions(): String {
    val maxHeight = Config.instance.maxMemeHeight
    val maxWidth = Config.instance.maxMemeWidth
    val filePath = visualMeme.filePath
    return getCappingStyle(maxHeight, maxWidth, filePath)
  }

  private fun positionMemePanel(settings: MemePanelSettings, width: Int, height: Int) {
    val (x, y) = getPosition(
      settings.anchor,
      rootPane.x + rootPane.width,
      rootPane.y + rootPane.height,
      Rectangle(width, height)
    )
    setLocation(x, y)
  }

  private fun clear() {
    alpha = CLEARED_ALPHA
    overlay = null
  }

  private fun getPosition(
    anchor: NotificationAnchor,
    parentWidth: Int,
    parentHeight: Int,
    memePanelBoundingBox: Rectangle,
  ): Pair<Int, Int> = when (anchor) {
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
        else -> parentWidth - memePanelBoundingBox.width - NOTIFICATION_Y_OFFSET
      } to when (anchor) {
        TOP_LEFT, TOP_RIGHT -> NOTIFICATION_Y_OFFSET
        BOTTOM_LEFT, BOTTOM_RIGHT ->
          parentHeight - memePanelBoundingBox.height - NOTIFICATION_Y_OFFSET
        else -> (parentHeight - memePanelBoundingBox.height) / HALF_DIVISOR
      }
  }

  override fun paintChildren(g: Graphics?) {
    if (overlay == null || alpha == CLEARED_ALPHA) {
      super.paintChildren(g)
    }
  }

  override fun paintComponent(g: Graphics?) {
    super.paintComponent(g)
    if (g !is Graphics2D) return

    if (overlay == null && alpha != CLEARED_ALPHA) {
      initComponentImage()
    }

    if (overlay != null && alpha != CLEARED_ALPHA) {
      g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
      StartupUiUtil.drawImage(g, overlay!!, 0, 0, null)
    }
  }

  private fun initComponentImage() {
    if (overlay != null) return

    overlay = UIUtil.createImage(this, width, height, BufferedImage.TYPE_INT_ARGB)
    UIUtil.useSafely(overlay!!.graphics) { imageGraphics: Graphics2D ->
      fancyPaintChildren(imageGraphics)
    }
  }

  private fun fancyPaintChildren(imageGraphics2d: Graphics2D) {
    // Paint to an image without alpha to preserve fonts subpixel antialiasing
    val image: BufferedImage = ImageUtil.createImage(
      imageGraphics2d,
      width,
      height,
      BufferedImage.TYPE_INT_RGB
    )

    val fillColor = MessageType.INFO.popupBackground
    UIUtil.useSafely(image.createGraphics()) { imageGraphics: Graphics2D ->
      imageGraphics.paint = Color(fillColor.rgb) // create a copy to remove alpha
      imageGraphics.fillRect(0, 0, width, height)
      super.paintChildren(imageGraphics)
    }

    val g2d = imageGraphics2d.create() as Graphics2D

    try {
      if (JreHiDpiUtil.isJreHiDPI(g2d)) {
        val s = 1 / JBUIScale.sysScale(g2d)
        g2d.scale(s.toDouble(), s.toDouble())
      }
      StartupUiUtil.drawImage(g2d, makeColorTransparent(image, fillColor), 0, 0, null)
    } finally {
      g2d.dispose()
    }
  }

  private fun makeColorTransparent(image: Image, color: Color): Image {
    val markerRGB = color.rgb or -0x1000000
    return ImageUtil.filter(
      image,
      object : RGBImageFilter() {
        override fun filterRGB(x: Int, y: Int, rgb: Int): Int =
          if (rgb or -0x1000000 == markerRGB) {
            WHITE_HEX and rgb // set alpha to 0
          } else rgb
      }
    )
  }

  /**
   * In short, the fade in/out animations work by first painting the
   * panel, taking an image still, then display the image over top and
   * perform the transparency to the image, so that it looks like it
   * fades in/out.
   */
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
        paintImmediately(0, 0, width, height)
      }

      override fun paintCycleEnd() {
        if (isForward) {
          clear()

          self.repaint()

          if (memePanelSettings.dismissal == TIMED) {
            setFadeOutTimer()
          }
          self.lifecycleListener.onDisplay()
        } else {
          removeMeme()
        }
        Disposer.dispose(this)
      }

      private fun setFadeOutTimer() {
        registerDelayedRequest(self.fadeoutAlarm, getMemeDuration()) {
          self.runAnimation(false)
        }
      }
    }

    animator.resume()
  }

  private fun removeMeme() {
    fadeoutAlarm.cancelAllRequests()
    rootPane.remove(this)
    rootPane.revalidate()
    rootPane.repaint()
    lifecycleListener.onRemoval()
    Disposer.dispose(this)
  }

  private fun getMemeDuration(): Int {
    val memeDisplayDuration = memePanelSettings.displayDuration * TENTH_OF_A_SECOND_MULTIPLICAND
    return if (visualMeme.filePath.toString().endsWith(".gif", ignoreCase = true)) {
      if (memePlayer != null && memePlayer.duration > 0) {
        memePlayer.duration.toInt()
      } else {
        val duration = GifService.getDuration(visualMeme.filePath)
        if (duration < memeDisplayDuration) {
          duration * (memeDisplayDuration / max(duration, 1))
        } else {
          duration
        }
      }
    } else {
      memeDisplayDuration
    }
  }

  override fun dispose() {
    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener)
    fadeoutAlarm.dispose()
    invulnerabilityAlarm.dispose()
  }
}
