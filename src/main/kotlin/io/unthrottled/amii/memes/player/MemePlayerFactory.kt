package io.unthrottled.amii.memes.player

import io.unthrottled.amii.assets.AudibleMemeContent
import org.apache.commons.io.FilenameUtils

object MemePlayerFactory {
  fun createPlayer(audibleAssetContent: AudibleMemeContent): MemePlayer =
    when (FilenameUtils.getExtension(audibleAssetContent.filePath.toString())) {
      "wav" -> ClipSoundPlayer(audibleAssetContent)
      else -> DummyPlayer()
    }
}
