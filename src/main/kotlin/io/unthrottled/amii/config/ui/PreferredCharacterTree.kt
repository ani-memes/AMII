package io.unthrottled.amii.config.ui

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.TreeExpander
import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ide.ui.search.SearchableOptionsRegistrar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.Ref
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.packageDependencies.ui.TreeExpansionMonitor
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTree.CheckboxTreeCellRenderer
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.FilterComponent
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import io.unthrottled.amii.assets.AnimeEntity
import io.unthrottled.amii.assets.CharacterEntity
import io.unthrottled.amii.assets.VisualEntityService
import io.unthrottled.amii.services.CharacterGatekeeper
import io.unthrottled.amii.tools.toOptional
import java.awt.BorderLayout
import java.util.ArrayList
import java.util.HashMap
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

data class CharacterData(
  val anime: AnimeEntity,
  val characters: List<CharacterEntity>
)

class PreferredCharacterTree {
  private val characterCheckStatus: MutableMap<String, Boolean> = HashMap()
  val component: JComponent = JPanel(BorderLayout())
  private val myTree: CheckboxTree = createTree()
  private val myFilter: FilterComponent = MyFilterComponent()
  private val toolbarPanel: JPanel = JPanel(BorderLayout())

  val tree: JTree
    get() = myTree

  private fun initTree() {
    myTree.selectionModel.addTreeSelectionListener {
      val path = it.path
      val userObject = (path.lastPathComponent as DefaultMutableTreeNode).userObject
      selectionChanged(userObject)
    }
    val scrollPane = ScrollPaneFactory.createScrollPane(myTree)
    toolbarPanel.add(myFilter, BorderLayout.CENTER)
    toolbarPanel.border = JBUI.Borders.emptyBottom(2)
    val group = DefaultActionGroup()
    val actionManager = CommonActionsManager.getInstance()
    val treeExpander: TreeExpander = DefaultTreeExpander(myTree)
    group.add(actionManager.createExpandAllAction(treeExpander, myTree))
    group.add(actionManager.createCollapseAllAction(treeExpander, myTree))
    toolbarPanel.add(
      ActionManager.getInstance().createActionToolbar("PreferredCharacterTree", group, true).component,
      BorderLayout.WEST
    )
    component.add(toolbarPanel, BorderLayout.NORTH)
    component.add(scrollPane, BorderLayout.CENTER)
    myFilter.reset()
    reset(copyAndSort(getCharacterList()))
  }

  private fun createTree() =
    CheckboxTree(
      object : CheckboxTreeCellRenderer(true) {
        override fun customizeRenderer(
          tree: JTree,
          value: Any,
          selected: Boolean,
          expanded: Boolean,
          leaf: Boolean,
          row: Int,
          hasFocus: Boolean
        ) {
          if (value !is CheckedTreeNode) return

          val attributes =
            if (value.userObject is CharacterData) SimpleTextAttributes.REGULAR_ATTRIBUTES
            else SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
          val text = getNodeText(value)
          val background = UIUtil.getTreeBackground(selected, true)
          UIUtil.changeBackGround(this, background)
          SearchUtil.appendFragments(
            myFilter.toOptional().map { it.filter }.orElse(null),
            text,
            attributes.style,
            attributes.fgColor,
            background,
            textRenderer
          )
        }
      },
      CheckedTreeNode(null)
    )

  private fun selectionChanged(selected: Any?) {
    // todo this
  }

  // todo: this
  fun filterModel(filter: String?, force: Boolean): List<CharacterData> {
    val list: List<CharacterData> = getCharacterList()
    if (filter.isNullOrEmpty()) {
      return list
    }

    var result: List<CharacterData> =
      getCharacterList {
        it.anime.name.contains(filter, ignoreCase = true) ||
          it.name.contains(filter, ignoreCase = true)
      }

    val filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter)
    if (force && result.isEmpty()) {
      if (filters.size > 1) {
        result = filterModel(filter, false)
      }
    }
    return result
  }

  fun filter(intentionsToShow: List<CharacterData>) {
    refreshCheckStatus(myTree.model.root as CheckedTreeNode)
    reset(copyAndSort(intentionsToShow))
  }

  fun reset() {
    characterCheckStatus.clear()
    reset(copyAndSort(getCharacterList()))
  }

  private fun getCharacterList(predicate: (CharacterEntity) -> Boolean = { true }) =
    VisualEntityService.instance.allCharacters
      .filter(predicate)
      .groupBy { it.anime }
      .map { CharacterData(it.key, it.value.sortedBy { character -> character.name }) }

  private fun reset(sortedCharacterData: List<CharacterData>) {
    val root = CheckedTreeNode(null)
    val treeModel = myTree.model as DefaultTreeModel
    sortedCharacterData.forEach { characterData ->
      val animeRoot = CheckedTreeNode(characterData.anime)
      characterData.characters.forEach { character ->
        val characterNode = CheckedTreeNode(character)
        treeModel.insertNodeInto(characterNode, animeRoot, animeRoot.childCount)
      }
      treeModel.insertNodeInto(animeRoot, root, root.childCount)
    }
    treeModel.setRoot(root)
    treeModel.nodeChanged(root)
    TreeUtil.expandAll(myTree)
    myTree.setSelectionRow(0)
  }

  fun selectIntention(familyName: String) {
    val child = findChildRecursively(root, familyName)
    if (child != null) {
      val path = TreePath(child.path)
      TreeUtil.selectPath(myTree, path)
    }
  }

  private val root: CheckedTreeNode
    get() = myTree.model.root as CheckedTreeNode

  fun apply() {
    val root = root
    apply(root)
  }

  private fun refreshCheckStatus(root: CheckedTreeNode) {
    when (val userObject = root.userObject) {
      is AnimeEntity -> characterCheckStatus[userObject.id] = root.isChecked
      is CharacterEntity -> characterCheckStatus[userObject.id] = root.isChecked
      else -> visitChildren(root) { refreshCheckStatus(it) }
    }
  }

  val isModified: Boolean
    get() = isModified(root)

  fun dispose() {
    myFilter.dispose()
  }

  var filter: String?
    get() = myFilter.filter
    set(filter) {
      myFilter.filter = filter
    }

  internal fun interface CheckedNodeVisitor {
    fun visit(node: CheckedTreeNode)
  }

  private inner class MyFilterComponent : FilterComponent("CHARACTER_FILTER_HISTORY", 10) {
    private val myExpansionMonitor = TreeExpansionMonitor.install(myTree)
    override fun filter() {
      val filter = filter
      if (filter.isNullOrEmpty().not() && !myExpansionMonitor.isFreeze) {
        myExpansionMonitor.freeze()
      }
      this@PreferredCharacterTree.filter(filterModel(filter, true))
      val expandedPaths = TreeUtil.collectExpandedPaths(
        myTree
      )
      (myTree.model as DefaultTreeModel).reload()
      TreeUtil.restoreExpandedPaths(myTree, expandedPaths)
      SwingUtilities.invokeLater {
        myTree.setSelectionRow(0)
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown {
          IdeFocusManager.getGlobalInstance().requestFocus(
            myTree, true
          )
        }
      }
      TreeUtil.expandAll(myTree)
      if (filter.isNullOrEmpty()) {
        TreeUtil.collapseAll(myTree, 0)
        myExpansionMonitor.restore()
      }
    }

    override fun onlineFilter() {
      val filter = filter
      if (filter != null && filter.isNotEmpty()) {
        if (!myExpansionMonitor.isFreeze) {
          myExpansionMonitor.freeze()
        }
      }
      this@PreferredCharacterTree.filter(filterModel(filter, true))
      TreeUtil.expandAll(myTree)
      if (filter == null || filter.isEmpty()) {
        TreeUtil.collapseAll(myTree, 0)
        myExpansionMonitor.restore()
      }
    }
  }

  companion object {
    private fun copyAndSort(intentionsToShow: List<CharacterData>): List<CharacterData> {
      val copy: MutableList<CharacterData> = ArrayList(intentionsToShow)
      copy.sortWith { data1: CharacterData, data2: CharacterData ->
//        val category1 = data1.characters
//        val category2 = data2.characters
//        val result = ArrayUtil.lexicographicCompare(category1, category2)
//        if (result != 0) {
//          return@Comparator result
//        }
        data1.anime.compareTo(data2.anime)
      }
      return copy
    }

    private fun findChildRecursively(node: TreeNode, name: String): CheckedTreeNode? {
      val found = Ref<CheckedTreeNode?>()
      visitChildren(node) { node1: CheckedTreeNode ->
        if (found.get() != null) return@visitChildren
        val userObject = node1.userObject
        if (userObject is CharacterData) {
          val text = getNodeText(node1)
          if (name == text) {
            found.set(node1)
          }
        } else {
          val child = findChildRecursively(node1, name)
          if (child != null) {
            found.set(child)
          }
        }
      }
      return found.get()
    }

    private fun getNodeText(node: CheckedTreeNode): String =
      when (val userObject = node.userObject) {
        is AnimeEntity -> userObject.name
        is CharacterEntity -> userObject.name
        else -> "???"
      }

    // todo: this
    @JvmStatic
    private fun apply(root: CheckedTreeNode) {
      val userObject = root.userObject
      if (userObject is CharacterData) {
        // something
      } else {
        visitChildren(root) { apply(it) }
      }
    }

    private fun isModified(root: CheckedTreeNode): Boolean {
      val userObject = root.userObject
      return if (userObject is CharacterData) {
        val enabled = CharacterGatekeeper.instance.isPreferred(userObject.characters.first())
        enabled != root.isChecked
      } else {
        val modified = booleanArrayOf(false)
        visitChildren(
          root
        ) { node: CheckedTreeNode -> modified[0] = modified[0] or isModified(node) }
        modified[0]
      }
    }

    private fun visitChildren(
      node: TreeNode,
      visitor: CheckedNodeVisitor
    ) {
      val children = node.children()
      while (children.hasMoreElements()) {
        val child = children.nextElement() as CheckedTreeNode
        visitor.visit(child)
      }
    }
  }

  init {
    initTree()
  }
}
