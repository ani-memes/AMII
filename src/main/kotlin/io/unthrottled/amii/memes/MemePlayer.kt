package io.unthrottled.amii.memes

import io.unthrottled.amii.assets.AudibleMemeContent
import io.unthrottled.amii.memes.MemePlayer.Companion.NO_LENGTH
import org.apache.commons.io.FilenameUtils

interface MemePlayer {
  companion object {
    const val NO_LENGTH: Long = -1L
  }

  val duration: Long

  fun play()

  fun stop()
}

object MemePlayerFactory {

  fun createPlayer(audibleAssetContent: AudibleMemeContent): MemePlayer {
    val assetExtension = FilenameUtils.getExtension(audibleAssetContent.filePath.toString())
    return DummyPlayer()
  }
}

class DummyPlayer : MemePlayer {
  override val duration: Long
    get() = NO_LENGTH

  override fun play() {}

  override fun stop() {}
}
