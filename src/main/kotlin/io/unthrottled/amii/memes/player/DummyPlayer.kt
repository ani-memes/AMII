package io.unthrottled.amii.memes.player

class DummyPlayer : MemePlayer {
  override val duration: Long
    get() = MemePlayer.NO_LENGTH

  override fun play() {}

  override fun stop() {}
  override fun clone(): MemePlayer = this
}
