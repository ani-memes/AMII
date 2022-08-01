package io.unthrottled.amii.config.ui

import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.ui.popup.list.PopupListElementRenderer
import io.unthrottled.amii.assets.MemeAssetCategory
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JList

class MemeCategoryPopupElementRenderer(list: ListPopupImpl) : PopupListElementRenderer<MemeAssetCategory>(list) {

  override fun createItemComponent(): JComponent {
    createLabel()

    val panel = panel(BorderLayout()) {
      add(myTextLabel, BorderLayout.CENTER)
    }

    return layoutComponent(panel)
  }

  override fun customizeComponent(list: JList<out MemeAssetCategory>, lang: MemeAssetCategory, isSelected: Boolean) {
  }
}
