package io.unthrottled.amii.memes

import io.unthrottled.amii.assets.AudibleMemeContent
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.memes.MemePlayer.Companion.NO_LENGTH
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
import org.apache.commons.io.FilenameUtils
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

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

class ClipSoundPlayer(
  audibleAssetContent: AudibleMemeContent,
) : MemePlayer {

  companion object {
    private const val MILLS_DIVISOR = 1000
  }

  private val clip: Clip =
    AudioSystem.getAudioInputStream(audibleAssetContent.filePath.toURL())
      .use { inputStream ->
        val newClip = AudioSystem.getClip()
        newClip.open(inputStream)
        newClip.addLineListener {
          if (it.type == LineEvent.Type.STOP) {
            newClip.close()
          }
        }
        newClip
      }

  init {
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
    val range = gainControl.maximum - gainControl.minimum
    gainControl.value = range * Config.instance.volume + gainControl.minimum
  }

  override val duration: Long
    get() = clip.microsecondLength / MILLS_DIVISOR

  override fun play() {
    clip.start()
  }

  override fun stop() {
    clip.close()
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
      return duration ?: NO_LENGTH
    }

  override fun play() {
    TODO("Not yet implemented")
  }

  override fun stop() {
    TODO("Not yet implemented")
  }
}
