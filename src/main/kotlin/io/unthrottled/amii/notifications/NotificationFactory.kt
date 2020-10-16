package io.unthrottled.amii.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType.BALLOON
import com.intellij.notification.NotificationGroup
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Ref
import com.intellij.ui.BalloonLayoutData
import io.unthrottled.amii.config.Constants.PLUGIN_NAME
import io.unthrottled.amii.services.MyProjectService
import io.unthrottled.amii.tools.BalloonTools.fetchBalloonParameters
import io.unthrottled.amii.tools.runSafely

object NotificationFactory {

  private val notificationGroup = NotificationGroup(
    PLUGIN_NAME,
    BALLOON,
    false
  )

  @Suppress("MaxLineLength")
  fun dispatchNotification(project: Project): Notification {
    val updateNotification = notificationGroup.createNotification()
      .setTitle("his is a test")
      .setContent(
        """
            <img src='file:///home/alex/workspace/AMII/build/idea-sandbox/config/dokiThemeAssets/stickers/danganronpa/ibuki/dark/ibuki_dark.png'
                 alt='das image'/>
        """.trimIndent()
      )

    runSafely({
      val (ideFrame, notificationPosition) = fetchBalloonParameters(project)
      val balloon = NotificationsManagerImpl.createBalloon(
        ideFrame,
        updateNotification,
        true,
        true,
        createLayoutDataRef(),
        project.getService(MyProjectService::class.java) // todo: re-visit this
      )
      balloon.setAnimationEnabled(true)
      balloon.show(notificationPosition, Balloon.Position.atLeft)
    }) {
      updateNotification.notify(project)
    }

    return updateNotification
  }

  private fun createLayoutDataRef(): Ref<BalloonLayoutData> {
    val balloonLayoutData = BalloonLayoutData.createEmpty()
    balloonLayoutData.showFullContent = true
    balloonLayoutData.showMinSize = false
    balloonLayoutData.welcomeScreen = true
    return Ref(balloonLayoutData)
  }
}
