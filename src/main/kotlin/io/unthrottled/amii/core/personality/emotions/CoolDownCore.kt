package io.unthrottled.amii.core.personality.emotions

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.Alarm
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationType.COOL_DOWN
import io.unthrottled.amii.core.personality.emotions.MoodCategory.NEGATIVE
import java.util.concurrent.TimeUnit

internal enum class CoolDownStatus {
  COOLING_DOWN, COOL, UNINITIALIZED
}

class CoolDownCore : MoodListener, Disposable {
  companion object {
    private const val COOL_DOWN_DURATION_IN_MINUTES = 2L
  }

  private val coolDownAlarm = Alarm()
  private var coolDownStatus = CoolDownStatus.UNINITIALIZED

  private val spicyEmotions = setOf(Mood.AGITATED, Mood.ENRAGED, Mood.FRUSTRATED)

  override fun onDerivedMood(currentMood: Mood) {
    when (coolDownStatus) {
      CoolDownStatus.COOLING_DOWN -> handleMoodWhileInCoolDown(currentMood)
      CoolDownStatus.COOL, CoolDownStatus.UNINITIALIZED -> handleMoodWhileChill(currentMood)
    }
  }

  private fun handleMoodWhileChill(currentMood: Mood) {
    if (spicyEmotions.contains(currentMood)) {
      initiateNegativeEmotionCoolDown()
    }
  }

  private fun initiateNegativeEmotionCoolDown() {
    coolDownStatus = CoolDownStatus.COOLING_DOWN
    registerCoolDownEvent()
  }

  private fun registerCoolDownEvent() {
    coolDownAlarm.addRequest(
      {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EMOTIONAL_MUTATION_TOPIC)
          .onAction(EmotionalMutationAction(COOL_DOWN, NEGATIVE))
      },
      TimeUnit.MILLISECONDS.convert(
        COOL_DOWN_DURATION_IN_MINUTES,
        TimeUnit.MINUTES
      )
    )
  }

  private fun handleMoodWhileInCoolDown(currentMood: Mood) {
    coolDownAlarm.cancelAllRequests()
    if (currentMood != Mood.CALM) {
      registerCoolDownEvent()
    }
  }

  override fun dispose() {
    coolDownAlarm.dispose()
  }
}
