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

class GrazieLanguagesComponent() : GrazieUIComponent {
  private val languages = GrazieLanguagesList() {
    updateLinkToDownloadMissingLanguages()
  }

//  private val link: LinkLabel<Any?> =
//    LinkLabel<Any?>(msg("grazie.notification.missing-languages.action"), AllIcons.General.Warning).configure {
//      border = padding(JBUI.insetsTop(10))
//      setListener({ _, _ ->
// //        GrazieConfig.get().missedLanguages.forEach {
// //          // todo: something here...
// //        }
//      }, null)
//    }

  override val component: JPanel = panel {
    add(languages, BorderLayout.CENTER)
//    add(link, BorderLayout.SOUTH)
  }

  fun updateLinkToDownloadMissingLanguages() {
//    link.isVisible = GrazieConfig.get().hasMissedLanguages()
  }

  override fun isModified(state: MemeCategoryState): Boolean {
    return languages.isModified(state)
  }

  override fun reset(state: MemeCategoryState) {
    updateLinkToDownloadMissingLanguages()
    languages.reset(state)
  }

  override fun apply(state: MemeCategoryState): MemeCategoryState {
    return languages.apply(state)
  }

  fun onUpdate(onUpdateListener: Consumer<Set<MemeAssetCategory>>) {
    // todo: this
  }
}
