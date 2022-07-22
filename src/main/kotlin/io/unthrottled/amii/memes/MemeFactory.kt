package io.unthrottled.amii.memes

import com.intellij.openapi.project.Project
import io.unthrottled.amii.assets.MemeAsset
import io.unthrottled.amii.tools.getRootPane
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

fun Project.memeFactory(): MemeFactory = this.getService(MemeFactory::class.java)
class MemeFactory(private val project: Project) {

  fun getMemeBuilderForAsset(memeAsset: MemeAsset): Optional<Meme.Builder> {
    return project.getRootPane()
      .toOptional()
      .map { rootPane ->
        Meme.Builder(
          memeAsset.visualMemeContent,
          memeAsset.audibleMemeContent,
          rootPane,
          project
        )
      }
  }
}
