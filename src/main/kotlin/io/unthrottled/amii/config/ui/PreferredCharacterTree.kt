package io.unthrottled.amii.config.ui

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.IntentionManager
import com.intellij.codeInsight.intention.impl.config.IntentionActionMetaData
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.codeInsight.intention.impl.config.IntentionManagerSettings
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.TreeExpander
import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ide.ui.search.SearchableOptionsRegistrar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.packageDependencies.ui.TreeExpansionMonitor
import com.intellij.psi.PsiFile
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTree.CheckboxTreeCellRenderer
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.FilterComponent
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ArrayUtil
import com.intellij.util.TimeoutUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import java.awt.BorderLayout
import java.util.ArrayList
import java.util.HashMap
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class PreferredCharacterTree {
  private val myIntentionToCheckStatus: MutableMap<IntentionActionMetaData, Boolean> = HashMap()
  var component: JComponent? = null
    private set
  private var myTree: CheckboxTree? = null
  private var myFilter: FilterComponent? = null
  private var toolbarPanel: JPanel? = null
  val tree: JTree?
    get() = myTree

  private fun initTree() {
    myTree = CheckboxTree(object : CheckboxTreeCellRenderer(true) {
      override fun customizeRenderer(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
      ) {
        if (value !is CheckedTreeNode) {
          return
        }
        val node = value
        val attributes =
          if (node.userObject is IntentionActionMetaData) SimpleTextAttributes.REGULAR_ATTRIBUTES else SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
        val text = getNodeText(node)
        val background = UIUtil.getTreeBackground(selected, true)
        UIUtil.changeBackGround(this, background)
        SearchUtil.appendFragments(
          if (myFilter != null) myFilter!!.filter else null,
          text,
          attributes.style,
          attributes.fgColor,
          background,
          textRenderer
        )
      }
    }, CheckedTreeNode(null))
    myTree!!.selectionModel.addTreeSelectionListener { e: TreeSelectionEvent ->
      val path = e.path
      val userObject = (path.lastPathComponent as DefaultMutableTreeNode).userObject
      selectionChanged(userObject)
    }
    myFilter = MyFilterComponent()
    component = JPanel(BorderLayout())
    val scrollPane = ScrollPaneFactory.createScrollPane(myTree)
    toolbarPanel = JPanel(BorderLayout())
    toolbarPanel!!.add(myFilter, BorderLayout.CENTER)
    toolbarPanel!!.border = JBUI.Borders.emptyBottom(2)
    val group = DefaultActionGroup()
    val actionManager = CommonActionsManager.getInstance()
    val treeExpander: TreeExpander = DefaultTreeExpander(myTree!!)
    group.add(actionManager.createExpandAllAction(treeExpander, myTree))
    group.add(actionManager.createCollapseAllAction(treeExpander, myTree))
    toolbarPanel!!.add(
      ActionManager.getInstance().createActionToolbar("IntentionSettingsTree", group, true).component,
      BorderLayout.WEST
    )
    component?.add(toolbarPanel, BorderLayout.NORTH)
    component?.add(scrollPane, BorderLayout.CENTER)
    myFilter!!.reset()
  }

  private fun selectionChanged(selected: Any?) {
    // todo this
  }

  fun filterModel(filter: String?, force: Boolean): List<IntentionActionMetaData> {
    val list: List<IntentionActionMetaData> = getMetaData()
    if (filter.isNullOrEmpty()) {
      return list
    }

    var result: List<IntentionActionMetaData> = ArrayList(list)

    val filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter)
    if (force && result.isEmpty()) {
      if (filters.size > 1) {
        result = filterModel(filter, false)
      }
    }
    return result
  }

  private fun getMetaData(): List<IntentionActionMetaData> {
    val intentionAction: IntentionAction = object : IntentionAction {
      override fun getText(): @IntentionName String {
        return "Ryuko"
      }

      override fun getFamilyName(): @IntentionFamilyName String {
        return "KillLaKill"
      }

      override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return true
      }

      override fun invoke(project: Project, editor: Editor, file: PsiFile) {
      }

      override fun startInWriteAction(): Boolean {
        return false
      }
    }
    val intentionActionMetaData = IntentionActionMetaData(
      intentionAction, this.javaClass.classLoader, arrayOf(), "Ayy lmao"
    )
    return listOf(intentionActionMetaData)
  }

  fun filter(intentionsToShow: List<IntentionActionMetaData>) {
    refreshCheckStatus(myTree!!.model.root as CheckedTreeNode)
    reset(copyAndSort(intentionsToShow))
  }

  fun reset() {
    val intentionManager = IntentionManager.getInstance() as IntentionManagerImpl
    while (intentionManager.hasActiveRequests()) {
      TimeoutUtil.sleep(100)
    }
    val intentionManagerSettings = IntentionManagerSettings.getInstance()
    myIntentionToCheckStatus.clear()
    val intentions = intentionManagerSettings.metaData
    for (metaData in intentions) {
      myIntentionToCheckStatus[metaData] = intentionManagerSettings.isEnabled(metaData)
    }
    reset(copyAndSort(intentions))
  }

  private fun reset(sortedIntentions: List<IntentionActionMetaData>) {
    val root = CheckedTreeNode(null)
    val treeModel = myTree!!.model as DefaultTreeModel
    for (metaData in sortedIntentions) {
      var node: CheckedTreeNode? = root
      for (name in metaData.myCategory) {
        var child: CheckedTreeNode? = findChild(node, name)
        if (child == null) {
          val newChild = CheckedTreeNode(name)
          treeModel.insertNodeInto(newChild, node, node!!.childCount)
          child = newChild
        }
        node = child
      }
      treeModel.insertNodeInto(CheckedTreeNode(metaData), node, node!!.childCount)
    }
    resetCheckMark(root)
    treeModel.setRoot(root)
    treeModel.nodeChanged(root)
    TreeUtil.expandAll(myTree!!)
    myTree!!.setSelectionRow(0)
  }

  fun selectIntention(familyName: String) {
    val child = findChildRecursively(root, familyName)
    if (child != null) {
      val path = TreePath(child.path)
      TreeUtil.selectPath(myTree!!, path)
    }
  }

  private val root: CheckedTreeNode
    get() = myTree!!.model.root as CheckedTreeNode

  private fun resetCheckMark(root: CheckedTreeNode): Boolean {
    val userObject = root.userObject
    return if (userObject is IntentionActionMetaData) {
      val b = myIntentionToCheckStatus[userObject]
      val enabled = b === java.lang.Boolean.TRUE
      root.isChecked = enabled
      enabled
    } else {
      root.isChecked = false
      visitChildren(root) { node: CheckedTreeNode ->
        if (resetCheckMark(node)) {
          root.isChecked = true
        }
      }
      root.isChecked
    }
  }

  fun apply() {
    val root = root
    apply(root)
  }

  private fun refreshCheckStatus(root: CheckedTreeNode) {
    val userObject = root.userObject
    if (userObject is IntentionActionMetaData) {
      myIntentionToCheckStatus[userObject] = root.isChecked
    } else {
      visitChildren(root) { root: CheckedTreeNode -> refreshCheckStatus(root) }
    }
  }

  val isModified: Boolean
    get() = isModified(root)

  fun dispose() {
    myFilter!!.dispose()
  }

  var filter: String?
    get() = myFilter!!.filter
    set(filter) {
      myFilter!!.filter = filter
    }

  internal fun interface CheckedNodeVisitor {
    fun visit(node: CheckedTreeNode)
  }

  private inner class MyFilterComponent() : FilterComponent("INTENTION_FILTER_HISTORY", 10) {
    private val myExpansionMonitor = TreeExpansionMonitor.install(myTree)
    override fun filter() {
      val filter = filter
      if (filter != null && filter.isNotEmpty()) {
        if (!myExpansionMonitor.isFreeze) {
          myExpansionMonitor.freeze()
        }
      }
      this@PreferredCharacterTree.filter(filterModel(filter, true))
      if (myTree != null) {
        val expandedPaths = TreeUtil.collectExpandedPaths(
          myTree!!
        )
        (myTree!!.model as DefaultTreeModel).reload()
        TreeUtil.restoreExpandedPaths(myTree!!, expandedPaths)
      }
      SwingUtilities.invokeLater {
        myTree!!.setSelectionRow(0)
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown {
          IdeFocusManager.getGlobalInstance().requestFocus(
            myTree!!, true
          )
        }
      }
      TreeUtil.expandAll(myTree!!)
      if (filter == null || filter.length == 0) {
        TreeUtil.collapseAll(myTree!!, 0)
        myExpansionMonitor.restore()
      }
    }

    override fun onlineFilter() {
      val filter = filter
      if (filter != null && filter.length > 0) {
        if (!myExpansionMonitor.isFreeze) {
          myExpansionMonitor.freeze()
        }
      }
      this@PreferredCharacterTree.filter(filterModel(filter, true))
      TreeUtil.expandAll(myTree!!)
      if (filter == null || filter.length == 0) {
        TreeUtil.collapseAll(myTree!!, 0)
        myExpansionMonitor.restore()
      }
    }
  }

  companion object {
    private fun copyAndSort(intentionsToShow: List<IntentionActionMetaData>): List<IntentionActionMetaData> {
      val copy: MutableList<IntentionActionMetaData> = ArrayList(intentionsToShow)
      copy.sortWith(java.util.Comparator { data1: IntentionActionMetaData, data2: IntentionActionMetaData ->
        val category1 = data1.myCategory
        val category2 = data2.myCategory
        val result = ArrayUtil.lexicographicCompare(category1, category2)
        if (result != 0) {
          return@Comparator result
        }
        data1.family.compareTo(data2.family)
      })
      return copy
    }

    private fun findChild(node: TreeNode?, name: String): CheckedTreeNode {
      val found = Ref<CheckedTreeNode>()
      visitChildren(node) { node1: CheckedTreeNode ->
        val text = getNodeText(node1)
        if (name == text) {
          found.set(node1)
        }
      }
      return found.get()
    }

    private fun findChildRecursively(node: TreeNode, name: String): CheckedTreeNode? {
      val found = Ref<CheckedTreeNode?>()
      visitChildren(node, { node1: CheckedTreeNode ->
        if (found.get() != null) return@visitChildren
        val userObject = node1.userObject
        if (userObject is IntentionActionMetaData) {
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
      })
      return found.get()
    }

    private fun getNodeText(node: CheckedTreeNode): String {
      val userObject = node.userObject
      val text: String
      text = if (userObject is String) {
        userObject
      } else if (userObject is IntentionActionMetaData) {
        userObject.family
      } else {
        "???"
      }
      return text
    }

    // todo: this
    @JvmStatic
    private fun apply(root: CheckedTreeNode) {
      val userObject = root.userObject
      if (userObject is IntentionActionMetaData) {
        // something
      } else {
        visitChildren(root) { root: CheckedTreeNode -> apply(root) }
      }
    }

    private fun isModified(root: CheckedTreeNode): Boolean {
      val userObject = root.userObject
      return if (userObject is IntentionActionMetaData) {
        val enabled = IntentionManagerSettings.getInstance().isEnabled(userObject)
        enabled != root.isChecked
      } else {
        val modified = booleanArrayOf(false)
        visitChildren(
          root
        ) { node: CheckedTreeNode -> modified[0] = modified[0] or isModified(node) }
        modified[0]
      }
    }

    private fun visitChildren(node: TreeNode?, visitor: CheckedNodeVisitor) {
      val children = node!!.children()
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
