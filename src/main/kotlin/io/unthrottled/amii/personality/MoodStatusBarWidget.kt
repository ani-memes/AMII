package io.unthrottled.amii.personality

import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import icons.AMIIIcons
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.config.ui.PluginSettingsUI
import io.unthrottled.amii.core.personality.emotions.EMOTION_TOPIC
import io.unthrottled.amii.core.personality.emotions.Mood
import io.unthrottled.amii.core.personality.emotions.MoodListener
import io.unthrottled.amii.tools.toOptional
import java.awt.event.MouseEvent
import java.util.Optional
import javax.swing.Icon

class MoodStatusBarWidget(private val project: Project) :
  StatusBarWidget,
  StatusBarWidget.IconPresentation {
  companion object {
    private const val ID = "io.unthrottled.amii.personality.MoodStatusBarWidget"
  }

  private val appMessageBusConnection = ApplicationManager.getApplication().messageBus.connect()
  private val projectMessageBusConnection = project.messageBus.connect()

  private lateinit var seenMood: Mood

  private val currentMood: Optional<Mood>
    get() = if (this::seenMood.isInitialized) {
      seenMood.toOptional()
    } else {
      Optional.empty()
    }

  init {
    appMessageBusConnection.subscribe(
      LafManagerListener.TOPIC,
      LafManagerListener {
        updateWidget()
      }
    )
    appMessageBusConnection.subscribe(
      ConfigListener.CONFIG_TOPIC,
      ConfigListener { updateWidget() }
    )
    projectMessageBusConnection.subscribe(
      EMOTION_TOPIC,
      object : MoodListener {
        override fun onDerivedMood(currentMood: Mood) {
          seenMood = currentMood
          updateWidget()
        }
      }
    )
    StartupManager.getInstance(project).runWhenProjectIsInitialized {
      project.messageBus.syncPublisher(EMOTION_TOPIC).onRequestMood()
      updateWidget()
    }
  }

  private fun updateWidget() {
    WindowManager.getInstance().getStatusBar(project).toOptional()
      .ifPresent {
        it.updateWidget(ID)
      }
  }

  override fun getTooltipText(): String = currentMood
    .filter { Config.instance.showMood }
    .map { it.displayValue }
    .map { "MIKU is $it." }
    .orElse("")

  override fun ID(): String = ID

  override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

  override fun install(statusBar: StatusBar) {
    statusBar.updateWidget(ID)
  }

  override fun dispose() {
    appMessageBusConnection.dispose()
    projectMessageBusConnection.dispose()
  }

  override fun getIcon(): Icon? =
    currentMood
      .filter { Config.instance.showMood }
      .map { getEmoji(it) }
      .orElse(null)

  private fun getEmoji(mood: Mood): Icon {
    return when (mood) {
      Mood.ENRAGED -> AMIIIcons.E1F92C
      Mood.FRUSTRATED -> AMIIIcons.E1F620
      Mood.AGITATED -> AMIIIcons.E1F612
      Mood.HAPPY -> AMIIIcons.E1F60A
      Mood.RELIEVED -> AMIIIcons.E1F60C
      Mood.EXCITED -> AMIIIcons.E1F973
      Mood.SMUG -> AMIIIcons.E1F60F
      Mood.SHOCKED -> AMIIIcons.E1F632
      Mood.TIRED -> AMIIIcons.E1F634
      Mood.BORED -> AMIIIcons.E1F611
      Mood.DISAPPOINTED -> AMIIIcons.E1F62D
      Mood.MILDLY_DISAPPOINTED -> AMIIIcons.MILD_DISAPPOINTMENT
      else -> AMIIIcons.E1F642
    }
  }

  override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
    ShowSettingsUtil.getInstance()
      .showSettingsDialog(project, PluginSettingsUI::class.java)
  }
}
