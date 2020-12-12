package io.unthrottled.amii.memes

import io.unthrottled.amii.assets.AudibleMemeContent
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
import org.apache.commons.io.FilenameUtils
import javax.sound.sampled.AudioFileFormat

interface MemePlayer {
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
    get() = -1

  override fun play() {}

  override fun stop() {}
}

class ClipSoundPlayer(
  private val audibleAssetContent: AudibleMemeContent,
) : MemePlayer {

  override val duration: Long
    get() {
      TODO("NOT YET IMPLEMENTED")
    }

  override fun play() {
    TODO("Not yet implemented")
  }

  override fun stop() {
    TODO("Not yet implemented")
  }
}

class Mp3Player(
  private val audibleAssetContent: AudibleMemeContent
) : MemePlayer {

  override val duration: Long
    get() {
      val baseFileFormat: AudioFileFormat = MpegAudioFileReader().getAudioFileFormat(
        audibleAssetContent.filePath.toURL()
      )
      val duration = baseFileFormat.properties()["duration"] as Long?
      return duration ?: -1L
    }

  override fun play() {
    TODO("Not yet implemented")
  }

  override fun stop() {
    TODO("Not yet implemented")
  }
}
