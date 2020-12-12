package io.unthrottled.amii.memes

import io.unthrottled.amii.assets.AudibleMemeContent
import io.unthrottled.amii.config.Config
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

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
