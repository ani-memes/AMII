package io.unthrottled.amii.memes

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.IconManager
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import com.intellij.util.ui.StartupUiUtil
import com.intellij.util.ui.UIUtil
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.Icon
import javax.swing.JComponent

object Shadow {
  val Bottom = load("/icons/shadow/bottom.svg")
  val BottomLeft = load("/icons/shadow/bottomLeft.svg")
  val BottomRight = load("/icons/shadow/bottomRight.svg")
  val Left = load("/icons/shadow/left.svg")
  val Right = load("/icons/shadow/right.svg")
  val Top = load("/icons/shadow/top.svg")
  val TopLeft = load("/icons/shadow/topLeft.svg")
  val TopRight = load("/icons/shadow/topRight.svg")

  private fun load(path: String): Icon {
    return IconManager.getInstance().getIcon(path, Shadow::class.java)
  }
}

object ShadowPainter {

  val topLeftWidth = Shadow.TopLeft.iconWidth
  val topLeftHeight = Shadow.TopLeft.iconHeight
  private val topRightWidth = Shadow.TopRight.iconWidth
  private val halfTopRightWidth = topRightWidth / 2
  private val topRightHeight = Shadow.TopRight.iconHeight
  private val bottomRightWidth = Shadow.BottomRight.iconWidth
  private val bottomRightHeight = Shadow.BottomRight.iconHeight
  private val bottomLeftWidth = Shadow.BottomLeft.iconWidth
  private val bottomLeftHeight = Shadow.BottomLeft.iconHeight
  private val topWidth = Shadow.Top.iconWidth
  private val bottomWidth = Shadow.Bottom.iconWidth
  private val bottomHeight = Shadow.Bottom.iconHeight
  private val halfBottomHeight = bottomHeight / 2
  private val leftHeight = Shadow.Left.iconHeight
  private val rightHeight = Shadow.Right.iconHeight

  fun paintShadow(component: JComponent, g: Graphics) {
    val width = component.width
    val height = component.height

    drawLine(
      component = component,
      g = g,
      icon = Shadow.Top,
      componentLength = width,
      startShadowCornerLength = topLeftWidth,
      endShadowCornerLength = topRightWidth,
      shadowIconWidth = topWidth,
      lineStartingPoint = 0,
      horizontal = true
    )
    drawLine(
      component = component,
      g = g,
      icon = Shadow.Bottom,
      componentLength = width,
      startShadowCornerLength = bottomLeftWidth,
      endShadowCornerLength = bottomRightWidth,
      shadowIconWidth = bottomWidth,
      lineStartingPoint = height + halfBottomHeight,
      horizontal = true
    )
    drawLine(
      component = component,
      g = g,
      icon = Shadow.Left,
      componentLength = height,
      startShadowCornerLength = topLeftHeight,
      endShadowCornerLength = bottomLeftHeight,
      shadowIconWidth = leftHeight,
      lineStartingPoint = 0,
      horizontal = false
    )

    drawLine(
      component = component,
      g = g,
      icon = Shadow.Right,
      componentLength = height,
      startShadowCornerLength = topRightHeight,
      endShadowCornerLength = bottomRightHeight,
      shadowIconWidth = rightHeight,
      lineStartingPoint = width + halfTopRightWidth - 1,
      horizontal = false
    )
    Shadow.TopLeft.paintIcon(component, g, 0, 0)
    Shadow.TopRight.paintIcon(component, g, width, 0)
    Shadow.BottomRight.paintIcon(component, g, width, height - 1) // shift up corners by 1
    Shadow.BottomLeft.paintIcon(component, g, 0, height - 1)
  }

  @Suppress("LongParameterList")
  private fun drawLine(
    component: JComponent,
    g: Graphics,
    icon: Icon,
    componentLength: Int,
    startShadowCornerLength: Int,
    endShadowCornerLength: Int,
    shadowIconWidth: Int,
    lineStartingPoint: Int,
    horizontal: Boolean
  ) {
    val halfStartShadowCorner = startShadowCornerLength / 2
    val lineLengthNeeded = componentLength - halfStartShadowCorner - (endShadowCornerLength / 2) -
      if (!horizontal) 1 else 0 // needed because the bottom shadow corners are shifted up 1
    val iconsNeeded = lineLengthNeeded / shadowIconWidth
    val iconLineLength = shadowIconWidth * iconsNeeded
    val lastValue = startShadowCornerLength + iconLineLength
    if (horizontal) {
      for (i in startShadowCornerLength until lastValue step shadowIconWidth) {
        icon.paintIcon(component, g, i, lineStartingPoint)
      }
    } else {
      for (i in startShadowCornerLength..lastValue step shadowIconWidth) {
        icon.paintIcon(component, g, lineStartingPoint, i)
      }
    }
    if (iconLineLength < lineLengthNeeded) {
      val iconSnapshot = IconLoader.getIconSnapshot(icon)
      val image = IconUtil.toImage(iconSnapshot, ScaleContext.create(component))
      if (horizontal) {
        StartupUiUtil.drawImage(
          g,
          image,
          Rectangle(
            lastValue,
            lineStartingPoint,
            lineLengthNeeded - iconLineLength,
            iconSnapshot.iconHeight
          ),
          Rectangle(
            0,
            0,
            lineLengthNeeded - iconLineLength,
            iconSnapshot.iconHeight
          ),
          component
        )
      } else {
        UIUtil.drawImage(
          g,
          image,
          Rectangle(
            lineStartingPoint,
            lastValue + shadowIconWidth,
            iconSnapshot.iconWidth,
            lineLengthNeeded - iconLineLength
          ),
          Rectangle(
            0,
            0,
            iconSnapshot.iconWidth,
            lineLengthNeeded - iconLineLength
          ),
          component
        )
      }
    }
  }
}
