package io.unthrottled.amii.memes

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import icons.AMIIIcons
import io.unthrottled.amii.assets.VisualEntityRepository
import io.unthrottled.amii.assets.VisualMemeContent
import io.unthrottled.amii.config.getConfig
import io.unthrottled.amii.tools.PluginMessageBundle
import io.unthrottled.amii.tools.toOptional
import org.intellij.lang.annotations.Language
import java.net.URLEncoder

fun Project.memeInfoService(): MemeInfoService = this.getService(MemeInfoService::class.java)

class MemeInfoService(private val project: Project) {

  private val notificationGroup =
    NotificationGroupManager.getInstance()
      .getNotificationGroup("AMII Info")

  fun stopShowing() {
    ApplicationManager.getApplication().getConfig().infoOnClick = false
  }

  fun displayInfo(visualMemeContent: VisualMemeContent) {
    val stopShowingAction = object : NotificationAction(PluginMessageBundle.message("amii.meme.info.stop")) {
      override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        stopShowing()
        notification.expire()
      }
    }
    val visualAssetEntity = VisualEntityRepository.instance.visualAssetEntities[visualMemeContent.id] ?: return
    visualAssetEntity.toOptional()
      .filter {
        it.characters.none { character ->
          character.name.contains("Unknown ", ignoreCase = true)
        } &&
          it.characters.isNotEmpty()
      }
      .map {
        val animeShown = visualAssetEntity.characters
          .map { it.anime }
          .distinct()
          .map { it.name }
        val characters = visualAssetEntity.characters.map { it.name }
        val characterPluralization = if (characters.size > 1) "s" else ""

        @Language("HTML")
        val content = """<div>
      | <span>Anime: ${animeShown.joinToString(", ")}</span><br/>
      | <span>Character$characterPluralization: ${characters.joinToString(", ")}</span>
      |</div>""".trimMargin()

        notificationGroup.createNotification(
          content,
          NotificationType.INFORMATION
        ).addAction(
          object : NotificationAction(PluginMessageBundle.message("amii.meme.info.search")) {
            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
              val queryString = URLEncoder.encode(
                "${animeShown.joinToString(" ") { "\"$it\"" }} ${characters.joinToString(" ")}",
                Charsets.UTF_8
              )
              val queryParameters = "q=$queryString&oq=$queryString"
              val searchUrl = "https://google.com/search?$queryParameters"
              BrowserUtil.browse(searchUrl)
            }
          }
        )
          .addAction(
            stopShowingAction
          )
      }.orElseGet {
        @Language("HTML")
        val lulDunno = """
          |${PluginMessageBundle.message("amii.meme.info.dunno")}<br>
          |¯\_(ツ)_/¯
        """.trimMargin()
        notificationGroup.createNotification(
          lulDunno,
          NotificationType.INFORMATION
        )
          .addAction(stopShowingAction)
      }
      .setIcon(AMIIIcons.PLUGIN_ICON)
      .setTitle(PluginMessageBundle.message("amii.meme.info.title"))
      .setListener(NotificationListener.UrlOpeningListener(false))
      .notify(project)
  }
}
