package io.unthrottled.amii.events

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

data class UserEvent(
  val eventName: String,
  val project: Project
)

val EVENT_TOPIC: Topic<UserEventListener> =
  Topic(UserEventListener::class.java)

interface UserEventListener {

  fun onDispatch(userEvent: UserEvent)
}
