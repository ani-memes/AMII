package io.unthrottled.amii.config.ui

import javax.swing.JComponent

internal interface GrazieUIComponent {
  val component: JComponent

  /** Should return true, if state of component does not match passed state of GrazieConfig (hence, it was modified) */
  fun isModified(state: MemeCategoryState): Boolean

  /** Resets state (and view) of component to passed state of GrazieConfig */
  fun reset(state: MemeCategoryState)

  /** Applies changes from component to passed state of GrazieConfig and returns new version */
  fun apply(state: MemeCategoryState): MemeCategoryState

  /** View-only components, that can not be modified somehow */
  interface ViewOnly : GrazieUIComponent {
    override fun isModified(state: MemeCategoryState) = false
    override fun apply(state: MemeCategoryState) = state
    override fun reset(state: MemeCategoryState) {}
  }

  /** Components, that change representation, but delegate actual data handing to `impl` */
  interface Delegating : GrazieUIComponent {
    val impl: GrazieUIComponent

    override fun isModified(state: MemeCategoryState) = impl.isModified(state)
    override fun apply(state: MemeCategoryState) = impl.apply(state)
    override fun reset(state: MemeCategoryState) = impl.reset(state)
  }
}
