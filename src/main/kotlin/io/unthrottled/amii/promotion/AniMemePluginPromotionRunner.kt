package io.unthrottled.amii.promotion

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.concurrency.EdtScheduledExecutorService
import io.unthrottled.amii.tools.doOrElse
import io.unthrottled.amii.tools.toOptional
import java.util.concurrent.TimeUnit

enum class PromotionStatus {
  ACCEPTED, REJECTED, BLOCKED
}

data class PromotionResults(
  val status: PromotionStatus
)

object AniMemePromotionService {
  fun runPromotion(
    isNewUser: Boolean,
    promotionDefinition: PromotionDefinition,
    onPromotion: (PromotionResults) -> Unit,
    onReject: () -> Unit,
  ) {
    AniMemePluginPromotionRunner(isNewUser, promotionDefinition, onPromotion, onReject)
  }
}

class AniMemePluginPromotionRunner(
  private val isNewUser: Boolean,
  private val promotionDefinition: PromotionDefinition,
  private val onPromotion: (PromotionResults) -> Unit,
  private val onReject: () -> Unit
) : Runnable {

  init {
    run()
  }

  override fun run() {
    AniMemePluginPromotion.runPromotion(isNewUser, promotionDefinition, onPromotion, onReject)
  }
}

object AniMemePluginPromotion {
  fun runPromotion(
    isNewUser: Boolean,
    promotionDefinition: PromotionDefinition,
    onPromotion: (PromotionResults) -> Unit,
    onReject: () -> Unit,
  ) {
    ApplicationManager.getApplication().executeOnPooledThread {
      // download assets on non-awt thread
      val promotionAssets = PromotionAssets(isNewUser, promotionDefinition)

      // schedule code execution to run on the EDT thread
      // so we can suggest a window
      EdtScheduledExecutorService.getInstance().schedule(
        {
          ProjectManager.getInstance().openProjects
            .toOptional()
            .filter { it.isNotEmpty() }
            .map { it.first() }
            .map {
              WindowManager.getInstance().suggestParentWindow(
                it
              )
            }
            .doOrElse(
              {
                ApplicationManager.getApplication().invokeLater {
                  AniMemePromotionDialog(
                    promotionAssets,
                    promotionDefinition,
                    it!!,
                    onPromotion
                  ).show()
                }
              },
              onReject
            )
        },
        0,
        TimeUnit.SECONDS
      )
    }
  }
}
