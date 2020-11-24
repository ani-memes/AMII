package io.unthrottled.amii.config.ui

import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ClickListener
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.runSafelyWithResult
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseEvent
import java.util.function.Consumer
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JToggleButton
import kotlin.math.max

enum class NotificationAnchor {
  TOP_LEFT, TOP_CENTER, TOP_RIGHT,
  MIDDLE_LEFT, CENTER, MIDDLE_RIGHT,
  BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

  companion object {
    fun fromValue(value: String): NotificationAnchor =
      runSafelyWithResult({
        valueOf(value)
      }) { TOP_RIGHT }
  }
}

object AnchorPanelFactory {
  private const val ANCHOR_GRID_COUNT = 3
  private const val ANCHOR_PANEL_SIZE = 88
  private const val GRID_SPACING_GAP = 1

  @JvmStatic
  fun getAnchorPositionPanel(
    selectedAnchor: NotificationAnchor,
    onSelection: Consumer<NotificationAnchor>,
  ): JPanel {
    val anchorGroup = ButtonGroup()
    val anchorPanel = JPanel()

    initAnchorPanel(
      anchorPanel,
      anchorGroup,
      selectedAnchor,
      onSelection
    )

    val anchorPanelSize = Dimension(ANCHOR_PANEL_SIZE, ANCHOR_PANEL_SIZE)
    anchorPanel.size = anchorPanelSize
    anchorPanel.preferredSize = anchorPanelSize

    return anchorPanel
  }

  private fun initAnchorPanel(
    anchorPanel: JPanel,
    buttonGroup: ButtonGroup,
    selectedAnchor: NotificationAnchor,
    onSelection: Consumer<NotificationAnchor>,
  ) {
    val backgroundColor: Color = UIUtil.getListSelectionBackground(true)
    anchorPanel.layout = GridLayout(
      ANCHOR_GRID_COUNT,
      ANCHOR_GRID_COUNT,
      GRID_SPACING_GAP,
      GRID_SPACING_GAP
    )
    NotificationAnchor.values().forEach {
      val anchorButton = JRadioButton(it.toString(), it == selectedAnchor)
      anchorButton.toolTipText = StringUtil.capitalize(anchorButton.text.replace('-', ' '))
      buttonGroup.add(anchorButton)
      addClickablePanel(
        anchorPanel,
        anchorButton,
        backgroundColor
      ) {
        onSelection.accept(it)
      }
    }
  }

  private fun addClickablePanel(
    anchorPanel: JPanel,
    button: JToggleButton,
    color: Color,
    onSelection: () -> Unit
  ): JBPanelWithEmptyText {
    val anchorLocationPanel: JBPanelWithEmptyText = object : JBPanelWithEmptyText() {

      override fun getPreferredSize(): Dimension {
        val dimension = super.getSize()
        dimension.height = max(dimension.width, dimension.height)
        dimension.width = dimension.height
        return dimension
      }

      override fun getMinimumSize(): Dimension {
        return preferredSize
      }

      override fun getMaximumSize(): Dimension {
        return preferredSize
      }

      override fun getBackground(): Color {
        return if (button.isSelected) color else UIUtil.getPanelBackground()
      }

      override fun isOpaque(): Boolean {
        return true
      }
    }
    anchorLocationPanel.emptyText.clear()
    object : ClickListener() {
      override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
        button.isSelected = button is JRadioButton || !button.isSelected
        if (button.isSelected) {
          onSelection()
        }
        anchorPanel.invalidate()
        anchorPanel.repaint()
        return true
      }
    }.installOn(anchorLocationPanel)
    anchorLocationPanel.border = BorderFactory.createLineBorder(color)
    anchorPanel.add(anchorLocationPanel)
    return anchorLocationPanel
  }
}
