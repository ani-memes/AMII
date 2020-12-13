package io.unthrottled.amii.memes.player

import io.unthrottled.amii.assets.AudibleContent
import org.apache.commons.io.FilenameUtils

object MemePlayerFactory {
  fun createPlayer(audibleAssetContent: AudibleContent): MemePlayer =
    when (FilenameUtils.getExtension(audibleAssetContent.filePath.toString())) {
      "wav" -> ClipSoundPlayer(audibleAssetContent)
      else -> DummyPlayer()
    }
}
