package io.unthrottled.amii.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.util.messages.Topic
import io.unthrottled.amii.onboarding.UpdateNotification
import io.unthrottled.amii.platform.UpdateAssetsListener
import io.unthrottled.amii.tools.PluginMessageBundle

fun interface SyncedAssetsListener {

  companion object {
    val TOPIC = Topic.create("Syncronized assets", SyncedAssetsListener::class.java)
  }

  fun onSynced()
}


// todo: started sync message
class AssetSyncAction : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    val connect = ApplicationManager.getApplication().messageBus.connect()
    connect
      .subscribe(
        SyncedAssetsListener.TOPIC,
        SyncedAssetsListener {
          UpdateNotification.sendMessage(
            PluginMessageBundle.message("actions.sync.title"),
            PluginMessageBundle.message("actions.sync.message"),
            e.project
          )
          connect.dispose()
        }
      )

    ApplicationManager.getApplication().messageBus
      .syncPublisher(UpdateAssetsListener.TOPIC)
      .onRequestedUpdate()
  }
}
