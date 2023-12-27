package io.unthrottled.amii.promotion

import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.amii.promotion.AniMemePromotionService.runPromotion
import io.unthrottled.amii.promotion.AppService.getApplicationName
import io.unthrottled.amii.promotion.LedgerMaster.getInitialLedger
import io.unthrottled.amii.promotion.LedgerMaster.persistLedger
import io.unthrottled.amii.promotion.LockMaster.acquireLock
import io.unthrottled.amii.promotion.LockMaster.releaseLock
import io.unthrottled.amii.tools.toOptional
import java.time.Instant

object PromotionManager : PromotionManagerImpl()

open class PromotionManagerImpl {

  private val log = Logger.getInstance(PromotionManager::class.java)

  private var initialized = false

  private val promotionLedger: PromotionLedger = getInitialLedger()

  private val promotions = listOf(
    riderPromotion,
    androidPromotion
  )

  fun registerPromotion(
    newVersion: String,
    forceRegister: Boolean = false,
    isNewUser: Boolean = false
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
    promotions.firstOrNull {
      it.shouldInstall() && shouldPromote(it)
    }.toOptional()
      .ifPresent {
        setupPromotion(isNewUser, it)
      }
  }

  private fun setupPromotion(
    isNewUser: Boolean,
    promotionDefinition: PromotionDefinition
  ) {
    try {
      if (acquireLock(id)) {
        runPromotion(
          isNewUser,
          promotionDefinition,
          {
            promotionLedger.allowedToPromote = it.status != PromotionStatus.BLOCKED
            promotionLedger.seenPromotions[promotionDefinition.id] =
              Promotion(promotionDefinition.id, Instant.now(), it.status)
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

  private val id: String
    get() = getApplicationName()

  private fun shouldPromote(promotionDefinition: PromotionDefinition): Boolean =
    promotionLedger.allowedToPromote &&
      (
        promotionLedger.seenPromotions.containsKey(promotionDefinition.id).not() ||
          promotionLedger.seenPromotions[promotionDefinition.id]?.result == PromotionStatus.ACCEPTED
        )
}
