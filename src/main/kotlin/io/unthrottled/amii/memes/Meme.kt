package io.unthrottled.amii.memes

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import io.unthrottled.amii.assets.AudibleContent
import io.unthrottled.amii.assets.VisualMemeContent
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ui.NotificationAnchor
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.player.MemePlayer
import io.unthrottled.amii.memes.player.MemePlayerFactory
import io.unthrottled.amii.tools.toOptional
import javax.swing.JLayeredPane

enum class Comparison {
  GREATER, EQUAL, LESSER, UNKNOWN
}

interface MemeDisplayListener {
  companion object {
    val TOPIC = Topic.create("Meme Display Events", MemeDisplayListener::class.java)
  }

  fun onDisplay(visualMemeId: String)
}

interface MemeLifecycleListener {

  fun onDismiss() {}

  fun onDisplay() {}
}

class Meme(
  private val memePlayer: MemePlayer?,
  private val memePanel: MemePanel,
  val userEvent: UserEvent,
  private val comparator: (Meme) -> Comparison,
) : Disposable {

  class Builder(
    private val visualMemeContent: VisualMemeContent,
    private val audibleContent: AudibleContent?,
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

    fun withAnchor(newAnchor: NotificationAnchor): Builder {
      notificationAnchor = newAnchor
      return this
    }

    fun build(): Meme {
      val memePlayer = audibleContent.toOptional()
        .map { MemePlayerFactory.createPlayer(it) }
        .orElse(null)
      return Meme(
        memePlayer,
        MemePanel(
          rootPane,
          visualMemeContent,
          memePlayer,
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
  }

  fun display() {
    if (memePlayer != null) {
      ApplicationManager.getApplication().executeOnPooledThread {
        memePlayer.play()
      }
    }

    ApplicationManager.getApplication().invokeLater {
      ApplicationManager.getApplication().messageBus.syncPublisher(MemeDisplayListener.TOPIC)
        .onDisplay(memePanel.visualMeme.id)
      listeners.forEach {
        it.onDisplay()
      }
      memePanel.display {
        listeners.forEach {
          it.onDismiss()
        }
        memePlayer?.stop()
      }
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
