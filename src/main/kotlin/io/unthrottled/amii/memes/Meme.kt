package io.unthrottled.amii.memes

import io.unthrottled.amii.assets.VisualMemeAsset
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import javax.swing.JLayeredPane

class Meme(
  private val memePanel: MemePanel,
  val userEvent: UserEvent,
  private val comparator: (Meme) -> Int,
) : Comparable<Meme> {

  class Builder(
    private val visualMemeAsset: VisualMemeAsset,
    private val userEvent: UserEvent,
    private val rootPane: JLayeredPane,
  ) {
    private var notificationMode = Config.instance.notificationMode
    private var notificationAnchor = Config.instance.notificationAnchor
    private var memeDisplayInvulnerabilityDuration = Config.instance.memeDisplayInvulnerabilityDuration
    private var memeDisplayTimedDuration = Config.instance.memeDisplayTimedDuration
    private var memeComparator: (Meme) -> Int = { 0 }

    fun withComparator(newComparator: (Meme) -> Int): Builder {
      memeComparator = newComparator
      return this
    }

    fun build(): Meme =
      Meme(
        MemePanel(
          rootPane,
          visualMemeAsset,
          MemePanelSettings(
            notificationMode,
            notificationAnchor,
            memeDisplayInvulnerabilityDuration,
            memeDisplayTimedDuration,
          )
        ),
        userEvent,
        memeComparator
      )
  }

  fun display() {
    memePanel.display()
  }

  override fun compareTo(other: Meme): Int =
    comparator(other)
}
