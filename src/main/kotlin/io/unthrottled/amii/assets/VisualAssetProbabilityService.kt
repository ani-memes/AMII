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
import kotlin.math.floor
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

  private val seenAssetLedger = AssetObservationService.getInitialItem()

  private val random = java.util.Random()
  private val probabilityTools = ProbabilityTools(
    Random(System.currentTimeMillis())
  )

  fun pickAssetFromList(visualMemes: Collection<VisualAssetEntity>): Optional<VisualAssetEntity> {
    val seenTimes = visualMemes.map { getSeenCount(it) }
    val maxSeen = seenTimes.stream().mapToInt { it }.max().orElse(0)
    val totalItems = visualMemes.size
    return probabilityTools.pickFromWeightedList(
      visualMemes.map {
        val timesObserved = getSeenCount(it)
        it to 1 + (
          (
            abs(random.nextGaussian()) *
              totalItems.toDouble().pow(maxSeen - timesObserved)
            )
          ).toLong()
      }.shuffled(random)
    )
  }

  // give strong bias to assets that haven't
  // been seen by the user during the ledger cycle duration see:
  // <code>MAX_ALLOWED_DAYS_PERSISTED</code> in AssetObservationService for
  // more details
  private fun getSeenCount(it: VisualAssetEntity) =
    seenAssetLedger.assetSeenCounts.getOrDefault(it.id, 0)

  override fun dispose() {
    messageBusConnection.dispose()
    AssetObservationService.persistLedger(seenAssetLedger)
    IdeEventQueue.getInstance().removeIdleListener(this)
  }

  override fun onDisplay(visualMemeId: String) {
    seenAssetLedger.assetSeenCounts[visualMemeId] =
      getAssetSeenCount(visualMemeId) + 1
  }

  // This prevents newly seen assets from always being
  // biased to be shown to the user.
  private fun getAssetSeenCount(visualMemeId: String): Int {
    val seenAssets = seenAssetLedger.assetSeenCounts
    return if (seenAssets.containsKey(visualMemeId)) seenAssets[visualMemeId]!!
    else floor(seenAssets.entries.map { it.value }.average()).toInt()
  }

  override fun run() {
    val updatedLedger = AssetObservationService.persistLedger(seenAssetLedger)
    updatedLedger.assetSeenCounts.forEach { (assetId, assetSeenCount) ->
      seenAssetLedger.assetSeenCounts[assetId] = assetSeenCount
    }
  }
}
