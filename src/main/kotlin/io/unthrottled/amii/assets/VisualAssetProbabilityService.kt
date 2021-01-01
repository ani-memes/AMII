package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.memes.MemeDisplayListener
import io.unthrottled.amii.tools.ProbabilityTools
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
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

  private val random = Random(System.currentTimeMillis())
  private val probabilityTools = ProbabilityTools(
    random
  )

  fun pickAssetFromList(visualMemes: Collection<VisualAssetEntity>): Optional<VisualAssetEntity> {
    val seenTimes = visualMemes.map { seenVisualAssetEntities.getOrDefault(it.id, 0) }
    val maxSeen = seenTimes.max() ?: 0
    val totalItems = visualMemes.size + 2
    val twiceItems = totalItems.toDouble().pow(2.0).toInt()
    return probabilityTools.pickFromWeightedList(
      visualMemes.map {
        val timesObserved = seenVisualAssetEntities.getOrDefault(it.id, 0)
        it to if (timesObserved == 0) twiceItems // give higher priority to unseen items
        // give less weight to more frequently seen items
        else 1 + random.nextInt(1, totalItems) * (maxSeen - timesObserved)
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
