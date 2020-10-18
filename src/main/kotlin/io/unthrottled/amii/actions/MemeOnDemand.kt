package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import io.unthrottled.amii.events.EVENT_TOPIC
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.memes.MemeFactory

class MemeOnDemand : AnAction(), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    MemeFactory.dispatchMeme(e.project!!)
    ApplicationManager.getApplication().messageBus
      .syncPublisher(EVENT_TOPIC)
      .onDispatch(
        UserEvent("Show Random")
      )
  }
}
