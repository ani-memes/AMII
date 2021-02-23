package io.unthrottled.amii.memes.player

import io.unthrottled.amii.assets.AudibleContent
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.tools.runSafelyWithResult
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine
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

  @Suppress("MagicNumber")
  private val clip: Clip? =
    AudioSystem.getAudioInputStream(audibleAssetContent.filePath.toURL())
      .use { inputStream ->
        runSafelyWithResult({
          val newClip = AudioSystem.getClip()
          newClip.open(inputStream)
          newClip.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
              newClip.close()
            }
          }
          newClip
        }) {
          runSafelyWithResult({
            val format = AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              44100F,
              16, 2, 4,
              AudioSystem.NOT_SPECIFIED.toFloat(), true
            )
            val info = DataLine.Info(Clip::class.java, format)
            AudioSystem.getLine(info) as Clip
          }) {
            null
          }
        }
      }

  init {
    val gainControl = clip?.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
    gainControl.value = DECIBEL_MULTIPLICAND * log10(Config.instance.volume)
  }

  override val duration: Long
    get() = clip?.microsecondLength?.div(MILLS_DIVISOR) ?: -1L

  override fun play() {
    clip?.start()
  }

  override fun stop() {
    clip?.close()
  }
}
