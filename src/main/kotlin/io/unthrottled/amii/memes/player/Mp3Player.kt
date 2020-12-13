package io.unthrottled.amii.memes.player

import io.unthrottled.amii.assets.AudibleContent
import javazoom.jl.player.FactoryRegistry
import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader
import java.nio.file.Files
import java.nio.file.Paths
import javax.sound.sampled.AudioFileFormat

// todo: volume adjustment
class Mp3Player(
  private val audibleAssetContent: AudibleContent
) : MemePlayer {

  val player: AdvancedPlayer

  init {
    val audioDevice = FactoryRegistry.systemRegistry().createAudioDevice()
    player = Files.newInputStream(Paths.get(audibleAssetContent.filePath))
      .use { audioInputStream ->
        AdvancedPlayer(audioInputStream, audioDevice)
      }
    player.playBackListener = object : PlaybackListener() {
      override fun playbackFinished(evt: PlaybackEvent?) {
        evt?.source?.close()
        player.close()
      }
    }
  }

  override val duration: Long
    get() {
      val baseFileFormat: AudioFileFormat = MpegAudioFileReader().getAudioFileFormat(
        audibleAssetContent.filePath.toURL()
      )
      val duration = baseFileFormat.properties()["duration"] as Long?
      return duration ?: MemePlayer.NO_LENGTH
    }

  override fun play() {
    player.play()
  }

  override fun stop() {
    player.close()
  }
}
