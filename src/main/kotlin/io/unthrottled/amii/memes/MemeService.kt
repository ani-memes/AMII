package io.unthrottled.amii.memes

import com.intellij.openapi.project.Project
import io.unthrottled.amii.tools.getRootPane
import io.unthrottled.amii.tools.toOptional
import javax.swing.JLayeredPane

fun Project.memeService(): MemeService = this.getService(MemeService::class.java)
class MemeService(private val project: Project) {

  private fun showMeme(meme: Meme) {
    meme.display()
  }

  fun displayMeme(meme: Meme,) {
    // be paranoid about existing memes
    // hanging around for some reason https://github.com/ani-memes/AMII/issues/108
    project.getRootPane().toOptional().ifPresent { dismissAllMemesInPane(it) }

    showMeme(meme)
  }

  fun clearMemes() {
    project.getRootPane().toOptional()
      .ifPresent { rootPane ->
        dismissAllMemesInPane(rootPane)

        // be paranoid and try to remove things again
        if (rootPane.getComponentCountInLayer(MemePanel.PANEL_LAYER) > 0) {
          rootPane.getComponentsInLayer(MemePanel.PANEL_LAYER)
            .filterIsInstance<MemePanel>()
            .forEach {
              rootPane.remove(it)
            }
        }

        rootPane.revalidate()
        rootPane.repaint()
      }
  }

  private fun dismissAllMemesInPane(rootPane: JLayeredPane) {
    if (rootPane.getComponentCountInLayer(MemePanel.PANEL_LAYER) < 1) return

    rootPane.getComponentsInLayer(MemePanel.PANEL_LAYER)
      .filterIsInstance<MemePanel>()
      .forEach {
        it.dismiss()
      }
  }
}
