package io.unthrottled.amii.memes

import com.intellij.openapi.Disposable
import io.unthrottled.amii.assets.VisualMemeContent
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.events.UserEvent
import javax.swing.JLayeredPane

enum class Comparison {
  GREATER, EQUAL, LESSER, UNKNOWN
}

fun interface MemeLifecycleListener {

  fun onDismiss()
}

class Meme(
  private val memePanel: MemePanel,
  val userEvent: UserEvent,
  private val comparator: (Meme) -> Comparison,
) : Disposable {

  class Builder(
    private val visualMemeAsset: VisualMemeContent,
    private val userEvent: UserEvent,
    private val rootPane: JLayeredPane,
  ) {
    private var notificationMode = Config.instance.notificationMode
    private var notificationAnchor = Config.instance.notificationAnchor
    private var memeDisplayInvulnerabilityDuration = Config.instance.memeDisplayInvulnerabilityDuration
    private var memeDisplayTimedDuration = Config.instance.memeDisplayTimedDuration
    private var memeComparator: (Meme) -> Comparison = { Comparison.EQUAL }

    fun withComparator(newComparator: (Meme) -> Comparison): Builder {
      memeComparator = newComparator
      return this
    }

    fun withDismissalMode(newDismissalOption: PanelDismissalOptions): Builder {
      notificationMode = newDismissalOption
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
    memePanel.display {
      listeners.forEach { it.onDismiss() }
    }
  }

  private val listeners = mutableListOf<MemeLifecycleListener>()

  fun addListener(listener: MemeLifecycleListener) {
    listeners.add(listener)
  }

  fun compareTo(other: Meme): Comparison =
    comparator(other)

  fun dismiss() {
    memePanel.dismiss()
  }

  override fun dispose() {
    memePanel.dispose()
  }
}
