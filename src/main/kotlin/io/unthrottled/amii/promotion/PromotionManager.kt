package io.unthrottled.amii.promotion

import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.promotion.AniMemePromotionService.runPromotion
import io.unthrottled.amii.promotion.AppService.getApplicationName
import io.unthrottled.amii.promotion.AppService.isRiderPlatform
import io.unthrottled.amii.promotion.LedgerMaster.getInitialLedger
import io.unthrottled.amii.promotion.LedgerMaster.persistLedger
import io.unthrottled.amii.promotion.LockMaster.acquireLock
import io.unthrottled.amii.promotion.LockMaster.releaseLock
import io.unthrottled.amii.promotion.PluginService.canRiderExtensionBeInstalled
import io.unthrottled.amii.promotion.PluginService.isRiderExtensionInstalled
import java.time.Instant
import java.util.UUID

val ANI_MEME_PROMOTION_ID: UUID = UUID.fromString("ebd20408-f174-4fb0-bdd8-6bf81e3b5a1b")

object PromotionManager : PromotionManagerImpl()

open class PromotionManagerImpl {

  private val log = Logger.getInstance(PromotionManager::class.java)

  private var initialized = false

  private val promotionLedger: PromotionLedger = getInitialLedger()

  fun registerPromotion(
    newVersion: String,
    forceRegister: Boolean = false,
    isNewUser: Boolean = false,
  ) {
    if (initialized.not() || forceRegister) {
      promotionRegistry(newVersion, isNewUser)
      initialized = true
    }
  }

  private fun promotionRegistry(newVersion: String, isNewUser: Boolean) {
    val versionInstallDates = promotionLedger.versionInstallDates
    if (versionInstallDates.containsKey(newVersion).not()) {
      versionInstallDates[newVersion] = Instant.now()
      persistLedger(promotionLedger)
    }
    setupPromotion(isNewUser)
  }

  private fun setupPromotion(isNewUser: Boolean) {
    if (shouldRiderExtensionBeInstalled() && shouldPromote()) {
      try {
        if (acquireLock(id)) {
          runPromotion(
            isNewUser,
            {
              promotionLedger.allowedToPromote = it.status != PromotionStatus.BLOCKED
              promotionLedger.seenPromotions[ANI_MEME_PROMOTION_ID] =
                Promotion(ANI_MEME_PROMOTION_ID, Instant.now(), it.status)
              persistLedger(promotionLedger)
              releaseLock(id)
            }
          ) {
            releaseLock(id)
          }
        }
      } catch (e: Throwable) {
        log.warn("Unable to promote for raisins.", e)
      }
    }
  }

  private fun shouldRiderExtensionBeInstalled() =
    isRiderPlatform() &&
      isRiderExtensionInstalled().not() &&
      canRiderExtensionBeInstalled()

  private val id: String
    get() = getApplicationName()

  private fun shouldPromote(): Boolean =
    promotionLedger.allowedToPromote &&
      (
        promotionLedger.seenPromotions.containsKey(ANI_MEME_PROMOTION_ID).not() ||
          promotionLedger.seenPromotions[ANI_MEME_PROMOTION_ID]?.result == PromotionStatus.ACCEPTED
        )
}
