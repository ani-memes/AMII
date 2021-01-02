package io.unthrottled.amii.memes

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.assets.MemeAsset
import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.assets.MemeAssetService
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.services.ExecutionService
import io.unthrottled.amii.tools.AssetTools
import io.unthrottled.amii.tools.BalloonTools.getIDEFrame
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.doOrElse
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

fun Project.memeService(): MemeService = this.getService(MemeService::class.java)

class MemeService(private val project: Project) {

  fun createMeme(
    userEvent: UserEvent,
    memeAssetCategory: MemeAssetCategory,
    memeDecorator: (Meme.Builder) -> Meme
  ) {
    buildMeme(memeDecorator, userEvent) { MemeAssetService.getFromCategory(memeAssetCategory) }
  }

  fun createMemeFromCategories(
    userEvent: UserEvent,
    vararg memeAssetCategories: MemeAssetCategory,
    memeDecorator: (Meme.Builder) -> Meme = { it.build() }
  ) {
    buildMeme(memeDecorator, userEvent) { AssetTools.resolveAssetFromCategories(*memeAssetCategories) }
  }

  private fun buildMeme(
    memeDecorator: (Meme.Builder) -> Meme,
    userEvent: UserEvent,
    memeSupplier: () -> Optional<MemeAsset>
  ) {
    ExecutionService.executeAsynchronously {
      getIDEFrame(project)
        .flatMap {
          UIUtil.getRootPane(
            it.component
          ).toOptional()
        }
        .map { it.layeredPane }
        .flatMap { rootPane ->
          memeSupplier()
            .map { memeAssets ->
              memeDecorator(
                Meme.Builder(
                  memeAssets.visualMemeContent,
                  memeAssets.audibleMemeContent,
                  userEvent,
                  rootPane
                )
              )
            }
        }.doOrElse({
          attemptToDisplayMeme(it)
        }) {
          UpdateNotification.sendMessage(
            PluginMessageBundle.message("notification.no-memes.title", userEvent.eventName),
            PluginMessageBundle.message("notification.no-memes.body"),
            project
          )
        }
    }
  }

  private var displayedMeme: Meme? = null
  private fun attemptToDisplayMeme(meme: Meme) {
    val comparison = displayedMeme?.compareTo(meme) ?: Comparison.UNKNOWN
    if (comparison == Comparison.GREATER || comparison == Comparison.UNKNOWN) {
      displayedMeme?.dismiss()
      showMeme(meme)
    } else {
      meme.dispose()
    }
  }

  private fun showMeme(meme: Meme) {
    displayedMeme = meme
    meme.addListener {
      displayedMeme = null
    }
    meme.display()
  }
}
