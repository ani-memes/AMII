package io.unthrottled.amii.config.ui

import com.intellij.icons.AllIcons
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
import io.unthrottled.amii.assets.MemeAssetService
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Component
import java.util.Enumeration
import java.util.function.Consumer
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

internal fun StatusText.setEmptyTextPlaceholder(
  mainText: String,
  @Nls shortcutText: String,
  shortcutButton: Buttons,
  shortcutAction: () -> Unit
) {
  text = mainText
  appendSecondaryText(shortcutText, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES) { shortcutAction() }

  val shortcut = KeymapUtil.getShortcutsText(CommonActionsPanel.getCommonShortcut(shortcutButton).shortcuts)
  appendSecondaryText(" ($shortcut)", SimpleTextAttributes.GRAYED_ATTRIBUTES, null)
}

class MemeCategoriesSet :
  AddDeleteListPanel<MemeAssetCategory>(null, emptyList()), MemeCategoryUIComponent {

  companion object {
    private val ADD_WITH_DROPDOWN = LayeredIcon(AllIcons.General.Add, AllIcons.General.Dropdown)
  }

  private var onUpdateListener: Consumer<Set<MemeAssetCategory>> = Consumer {
  }

  private val decorator: ToolbarDecorator = MyToolbarDecorator(myList)
    .setAddAction { findItemToAdd() }
    .setAddIcon(ADD_WITH_DROPDOWN)
    .setToolbarPosition(ActionToolbarPosition.BOTTOM)
    .setRemoveAction {
      ListUtil.removeSelectedItems(myList)
      this.onUpdateListener.accept(myListModel.elements().toSet())
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

  @Suppress("MagicNumber")
  override fun getListCellRenderer(): ListCellRenderer<*> =
    ConfigurableListCellRenderer<MemeAssetCategory> { component, category ->
      component.configure {
        border = padding(JBUI.insets(5))
        text = category.prettyName
      }
    }

  override fun addElement(itemToAdd: MemeAssetCategory?) {
    itemToAdd ?: return
    removeExistedCategories(itemToAdd)
    val positionToInsert = -(
      myListModel.elements()
        .toList()
        .binarySearch(
          itemToAdd,
          Comparator.comparing(MemeAssetCategory::name)
        ) + 1
      )
    myListModel.add(positionToInsert, itemToAdd)
    myList.clearSelection()
    myList.setSelectedValue(itemToAdd, true)
    this.onUpdateListener.accept(
      myListModel.elements().toSet()
    )
  }

  override fun findItemToAdd(): MemeAssetCategory? {
    // remove already enabled categories
    val available = getCategoriesForPopup()

    val step = MemeCategoryPopupStep(
      msg("amii.settings.meme.categories.popup.title"),
      available,
      ::addElement
    )
    val menu = MyListPopup(step)

    decorator.actionsPanel?.getAnActionButton(Buttons.ADD)
      ?.preferredPopupPoint?.let(menu::show)

    return null
  }

  private fun getCategoriesForPopup(): List<MemeAssetCategory> {
    return MemeAssetCategory.sortedValues().filter {
      MemeAssetService.isImplemented(it)
    }
  }

  private fun removeExistedCategories(category: MemeAssetCategory) {
    val existingCategoriesToRemove = ArrayList<MemeAssetCategory>()
    for (existed in myListModel.elements()) {
      if (existed.value == category.value) {
        existingCategoriesToRemove.add(existed)
      }
    }

    for (toRemove in existingCategoriesToRemove) {
      myListModel.removeElement(toRemove)
    }
  }

  private class MyListPopup(step: MemeCategoryPopupStep) : ListPopupImpl(null, step) {
    override fun getListElementRenderer() = MemeCategoryPopupElementRenderer(this)
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
      val available = getCategoriesForPopup()
      actionsPanel.setEnabled(Buttons.ADD, list.isEnabled && available.isNotEmpty())
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
    myListModel.addAll(state.setStuff)
  }

  override fun apply(state: MemeCategoryState): MemeCategoryState {
    return state.copy(setStuff = myListModel.elements().toSet())
  }

  fun setAddAction(onUpdateListener: Consumer<Set<MemeAssetCategory>>) {
    this.onUpdateListener = onUpdateListener
  }
}

internal class ConfigurableListCellRenderer<T>(
  val configure: (
    DefaultListCellRenderer,
    T
  ) -> Unit
) : DefaultListCellRenderer() {
  override fun getListCellRendererComponent(
    list: JList<*>?,
    value: Any?,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    val component = super.getListCellRendererComponent(
      list,
      value,
      index,
      isSelected,
      cellHasFocus
    ) as DefaultListCellRenderer
    configure(component, value as T)
    return component
  }
}
fun <T> Enumeration<T>.toSet() = toList().toSet()
