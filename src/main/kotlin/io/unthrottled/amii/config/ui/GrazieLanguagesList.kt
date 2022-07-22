package io.unthrottled.amii.config.ui

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.AddDeleteListPanel
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.CommonActionsPanel.Buttons
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ListUtil
import com.intellij.ui.RowsDnDSupport
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import io.unthrottled.amii.assets.MemeAssetCategory
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Component
import java.util.Enumeration
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

internal fun StatusText.setEmptyTextPlaceholder(
  mainText: String,
  @Nls shortcutText: String,
  shortcutButton: CommonActionsPanel.Buttons,
  shortcutAction: () -> Unit
) {
  text = mainText
  appendSecondaryText(shortcutText, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES) { shortcutAction() }

  val shortcut = KeymapUtil.getShortcutsText(CommonActionsPanel.getCommonShortcut(shortcutButton).shortcuts)
  appendSecondaryText(" ($shortcut)", SimpleTextAttributes.GRAYED_ATTRIBUTES, null)
}

class GrazieLanguagesList(private val onLanguageRemoved: (lang: MemeAssetCategory) -> Unit) :
  AddDeleteListPanel<MemeAssetCategory>(null, emptyList()), GrazieUIComponent {

  private val decorator: ToolbarDecorator = MyToolbarDecorator(myList)
    .setAddAction { findItemToAdd() }
    .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
    .setToolbarPosition(ActionToolbarPosition.BOTTOM)
    .setRemoveAction {
      myList.selectedValuesList.forEach(onLanguageRemoved)
      ListUtil.removeSelectedItems(myList)
    }

  init {
    layout = BorderLayout()
    add(decorator.createPanel(), BorderLayout.CENTER)

    emptyText.setEmptyTextPlaceholder(
      mainText = msg("amii.settings.meme.categories.empty.text"),
      shortcutText = msg("amii.settings.meme.categories.empty.action"),
      shortcutButton = Buttons.ADD,
      shortcutAction = { addElement(findItemToAdd()) }
    )
  }

  override fun initPanel() {}

  override fun getListCellRenderer(): ListCellRenderer<*> =
    ConfigurableListCellRenderer<MemeAssetCategory> { component, lang ->
      component.configure {
        border = padding(JBUI.insets(5))
        text = lang.name
      }
    }

  override fun addElement(itemToAdd: MemeAssetCategory?) {
    itemToAdd ?: return
    removeExistedDialects(itemToAdd)
    val positionToInsert = -(myListModel.elements().toList().binarySearch(itemToAdd, Comparator.comparing(MemeAssetCategory::name)) + 1)
    myListModel.add(positionToInsert, itemToAdd)
    myList.clearSelection()
    myList.setSelectedValue(itemToAdd, true)
  }

  override fun findItemToAdd(): MemeAssetCategory? {
    // remove already enabled languages and their dialects
    val (available, toDownload) = getLangsForPopup()

    val step = GrazieLanguagesPopupStep(
      msg("amii.settings.meme.categories.popup.title"), available, toDownload,
      ::addElement
    )
    val menu = MyListPopup(step)

    decorator.actionsPanel?.getAnActionButton(Buttons.ADD)?.preferredPopupPoint?.let(menu::show)

    return null
  }

  /** Returns pair of (available languages, languages to download) */
  private fun getLangsForPopup(): Pair<List<MemeAssetCategory>, List<MemeAssetCategory>> {
    val enabledLangs = myListModel.elements().asSequence().map { it.name }.toSet()
    val (available, toDownload) = MemeAssetCategory.sortedValues().filter { it.name !in enabledLangs }.partition { true }
    return available to toDownload
  }

  private fun removeExistedDialects(lang: MemeAssetCategory) {
    val dialectsToRemove = ArrayList<MemeAssetCategory>()
    for (existed in myListModel.elements()) {
//      if (existed.iso == lang.iso) {
//        dialectsToRemove.add(existed)
//      }
    }

    for (toRemove in dialectsToRemove) {
      myListModel.removeElement(toRemove)
    }
  }

  private class MyListPopup(step: GrazieLanguagesPopupStep) : ListPopupImpl(null, step) {
    override fun getListElementRenderer() = GrazieLanguagesPopupElementRenderer(this)
  }

  private inner class MyToolbarDecorator(private val list: JBList<MemeAssetCategory>) : ToolbarDecorator() {
    init {
      myRemoveActionEnabled = true
      myAddActionEnabled = true

      list.configure {
        addListSelectionListener { updateButtons() }
        addPropertyChangeListener("enabled") { updateButtons() }
      }
    }

    override fun updateButtons() {
      val (available, download) = getLangsForPopup()
      actionsPanel.setEnabled(Buttons.ADD, list.isEnabled && (available.isNotEmpty() || download.isNotEmpty()))
      actionsPanel.setEnabled(Buttons.REMOVE, !list.isSelectionEmpty)
      updateExtraElementActions(!list.isSelectionEmpty)
    }

    override fun setVisibleRowCount(rowCount: Int): MyToolbarDecorator {
      list.visibleRowCount = rowCount
      return this
    }

    override fun getComponent() = list

    override fun installDnDSupport() = RowsDnDSupport.install(list, list.model as EditableModel)

    override fun isModelEditable() = true
  }

  override val component: JComponent
    get() = this

  override fun isModified(state: MemeCategoryState): Boolean {
    return myListModel.elements().toSet() != state.setStuff
  }

  override fun reset(state: MemeCategoryState) {
    myListModel.clear()
//    GrazieConfig.get().enabledLanguages.sortedBy { it.name }.forEach {
//      myListModel.addElement(it)
//    }
  }

  override fun apply(state: MemeCategoryState): MemeCategoryState {
    return state.copy(setStuff = myListModel.elements().toSet())
  }
}

internal class ConfigurableListCellRenderer<T>(val configure: (DefaultListCellRenderer, T) -> Unit) : DefaultListCellRenderer() {
  override fun getListCellRendererComponent(
    list: JList<*>?,
    value: Any?,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as DefaultListCellRenderer
    configure(component, value as T)
    return component
  }
}

fun <T> Enumeration<T>.toSet() = toList().toSet()
