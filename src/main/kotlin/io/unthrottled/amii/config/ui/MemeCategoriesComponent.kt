package io.unthrottled.amii.config.ui

import io.unthrottled.amii.assets.MemeAssetCategory
import java.awt.BorderLayout
import java.util.function.Consumer
import javax.swing.JComponent
import javax.swing.JPanel

internal fun <T : JComponent> T.configure(configure: T.() -> Unit): T {
  this.configure()
  return this
}

class MemeCategoriesComponent() : MemeCategoryUIComponent {
  private val memeCategorySet = MemeCategoriesSet()


  override val component: JPanel = panel {
    add(memeCategorySet, BorderLayout.CENTER)
  }


  override fun isModified(state: MemeCategoryState): Boolean {
    return memeCategorySet.isModified(state)
  }

  override fun reset(state: MemeCategoryState) {
    memeCategorySet.reset(state)
  }

  override fun apply(state: MemeCategoryState): MemeCategoryState {
    return memeCategorySet.apply(state)
  }

  fun onUpdate(onUpdateListener: Consumer<Set<MemeAssetCategory>>) {
    memeCategorySet.setAddAction(onUpdateListener)
  }
}
