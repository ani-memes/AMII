package io.unthrottled.amii.config.ui

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.tools.DEFAULT_MESSAGE_BUNDLE
import io.unthrottled.amii.tools.PluginMessageBundle
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Insets
import java.awt.LayoutManager
import javax.swing.JPanel
import javax.swing.border.Border

internal fun panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
internal fun Container.panel(
  layout: LayoutManager = BorderLayout(0, 0),
  constraint: Any,
  body: JPanel.() -> Unit
): JPanel = JPanel(layout).apply(body).also { add(it, constraint) }

internal fun border(
  @Nls text: String,
  hasIndent: Boolean,
  insets: Insets,
  showLine: Boolean = true
): Border = IdeBorderFactory.createTitledBorder(text, hasIndent, insets).setShowLine(showLine)

internal fun padding(insets: Insets): Border = IdeBorderFactory.createEmptyBorder(insets)

@Nls
internal fun msg(@PropertyKey(resourceBundle = DEFAULT_MESSAGE_BUNDLE) key: String, vararg params: String): String {
  return PluginMessageBundle.message(key, *params)
}

object MemeCategoriesPanel {

  @JvmStatic
  fun createComponent(): Pair<JPanel, MemeCategoriesComponent> {
    val memeCategories = MemeCategoriesComponent()
    return panel(MigLayout(createLayoutConstraints())) {
      panel(MigLayout(createLayoutConstraints()), constraint = CC().growX().wrap()) {
        border = border(msg("amii.settings.meme.categories.title"), false, JBUI.insetsBottom(10), false)
        add(memeCategories.component, CC().width("350px").height("150px"))
      }

      memeCategories.reset(MemeCategoryState(emptySet()))
    } to memeCategories
  }
}

data class MemeCategoryState(
  val setStuff: Set<MemeAssetCategory>
)
