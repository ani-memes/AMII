package io.unthrottled.amii.events

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

enum class UserEvents {
  IDLE,
  LOGS,
  ON_DEMAND,
  PROCESS,
  STARTUP,
  TASK,
  TEST,
  RELAX,
}

enum class UserEventCategory {
  POSITIVE, NEGATIVE, NEUTRAL
}

data class UserEvent(
  val type: UserEvents,
  val category: UserEventCategory,
  val eventName: String,
  val project: Project
)

val EVENT_TOPIC: Topic<UserEventListener> =
  Topic(UserEventListener::class.java)

interface UserEventListener {

  fun onDispatch(userEvent: UserEvent)
}
