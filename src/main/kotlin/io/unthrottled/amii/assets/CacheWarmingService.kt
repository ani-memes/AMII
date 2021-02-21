package io.unthrottled.amii.assets

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.platform.UpdateAssetsListener
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class CacheWarmingService : Disposable, Runnable {

  companion object {
    val instance: CacheWarmingService
      get() = ApplicationManager.getApplication().getService(CacheWarmingService::class.java)
  }

  private var dateRequested = Instant.EPOCH

  init {
    IdeEventQueue.getInstance().addIdleListener(
      this,
      TimeUnit.MILLISECONDS.convert(
        Config.DEFAULT_IDLE_TIMEOUT_IN_MINUTES,
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  fun init() {
    // empty so that this registers the idle listener.
  }

  override fun dispose() {
    IdeEventQueue.getInstance().removeIdleListener(this)
  }

  override fun run() {
    val meow = Instant.now()
    if (Duration.between(dateRequested, meow).toDays() < 1) return

    dateRequested = meow

    ApplicationManager.getApplication().messageBus.syncPublisher(
      UpdateAssetsListener.TOPIC
    ).onRequestedBackgroundUpdate()
  }
}
