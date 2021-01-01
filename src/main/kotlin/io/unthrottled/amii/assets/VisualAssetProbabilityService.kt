package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.memes.MemeDisplayListener
import io.unthrottled.amii.tools.ProbabilityTools
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class VisualAssetProbabilityService : Disposable, MemeDisplayListener {

  companion object {
    val instance: VisualAssetProbabilityService
      get() = ApplicationManager.getApplication().getService(VisualAssetProbabilityService::class.java)
  }

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBusConnection.subscribe(MemeDisplayListener.TOPIC, this)
  }

  private val seenVisualAssetEntities = ConcurrentHashMap<String, Int>()

  private val random = java.util.Random()
  private val probabilityTools = ProbabilityTools(
    Random(System.currentTimeMillis())
  )

  fun pickAssetFromList(visualMemes: Collection<VisualAssetEntity>): Optional<VisualAssetEntity> {
    val seenTimes = visualMemes.map { seenVisualAssetEntities.getOrDefault(it.id, 0) }
    val maxSeen = seenTimes.max() ?: 0
    val totalItems = visualMemes.size
    return probabilityTools.pickFromWeightedList(
      visualMemes.map {
        val timesObserved = seenVisualAssetEntities.getOrDefault(it.id, 0)
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
  }

  override fun onDisplay(visualMemeId: String) {
    seenVisualAssetEntities[visualMemeId] =
      seenVisualAssetEntities.getOrDefault(visualMemeId, 0) + 1
  }
}
