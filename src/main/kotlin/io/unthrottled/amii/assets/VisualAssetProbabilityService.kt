package io.unthrottled.amii.assets

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.memes.MemeDisplayListener
import io.unthrottled.amii.tools.ProbabilityTools
import java.util.Optional
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class VisualAssetProbabilityService : Disposable, MemeDisplayListener, Runnable {

  companion object {
    val instance: VisualAssetProbabilityService
      get() = ApplicationManager.getApplication().getService(VisualAssetProbabilityService::class.java)
  }

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBusConnection.subscribe(MemeDisplayListener.TOPIC, this)
    IdeEventQueue.getInstance().addIdleListener(
      this,
      TimeUnit.MILLISECONDS.convert(
        Config.instance.idleTimeoutInMinutes,
        TimeUnit.MINUTES
      ).toInt()
    )
  }

  private val seenAssetLedger = AssetObservationService.getInitialLedger()

  private val random = java.util.Random()
  private val probabilityTools = ProbabilityTools(
    Random(System.currentTimeMillis())
  )

  fun pickAssetFromList(visualMemes: Collection<VisualAssetEntity>): Optional<VisualAssetEntity> {
    val seenTimes = visualMemes.map { seenAssetLedger.assetSeenCounts.getOrDefault(it.id, 0) }
    val maxSeen = seenTimes.max() ?: 0
    val totalItems = visualMemes.size
    return probabilityTools.pickFromWeightedList(
      visualMemes.map {
        val timesObserved = seenAssetLedger.assetSeenCounts.getOrDefault(it.id, 0)
        it to 1 + (
          (
            abs(random.nextGaussian()) *
              totalItems.toDouble().pow(maxSeen - timesObserved)
            )
          ).toLong()
      }.shuffled(random)
    )
  }

  override fun dispose() {
    messageBusConnection.dispose()
    AssetObservationService.persistLedger(seenAssetLedger)
    IdeEventQueue.getInstance().removeIdleListener(this)
  }

  override fun onDisplay(visualMemeId: String) {
    seenAssetLedger.assetSeenCounts[visualMemeId] =
      seenAssetLedger.assetSeenCounts.getOrDefault(visualMemeId, 0) + 1
  }

  override fun run() {
    AssetObservationService.persistLedger(seenAssetLedger)
  }
}
