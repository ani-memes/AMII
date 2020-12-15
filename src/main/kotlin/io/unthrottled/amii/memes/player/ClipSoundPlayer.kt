package io.unthrottled.amii.memes.player

import io.unthrottled.amii.assets.AudibleContent
import io.unthrottled.amii.config.Config
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.math.log10

class ClipSoundPlayer(
  audibleAssetContent: AudibleContent,
) : MemePlayer {

  companion object {
    private const val MILLS_DIVISOR = 1000
    private const val DECIBEL_MULTIPLICAND = 20F
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
    gainControl.value = DECIBEL_MULTIPLICAND * log10(Config.instance.volume)
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
