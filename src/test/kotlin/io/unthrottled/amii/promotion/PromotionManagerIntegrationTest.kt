package io.unthrottled.amii.promotion

import com.intellij.util.io.isFile
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.unthrottled.amii.assets.LocalStorageService
import io.unthrottled.amii.integrations.RestClient
import io.unthrottled.amii.tools.TestTools
import io.unthrottled.amii.tools.TestTools.setUpMocksForManager
import io.unthrottled.amii.tools.TestTools.tearDownMocksForPromotionManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.time.Instant
import java.time.Period
import java.util.UUID
import java.util.concurrent.TimeUnit

class PromotionManagerIntegrationTest {

  companion object {

    private val ANI_MEME_PROMOTION_ID: UUID = riderPromotion.id

    private const val testDirectory = "testOne"

    @JvmStatic
    @BeforeClass
    fun setUp() {
      setUpMocksForManager()
      mockkObject(AniMemePromotionService)
      mockkObject(RestClient)
      mockkObject(PluginService)
      mockkObject(AppService)
    }

    @JvmStatic
    @AfterClass
    fun tearDown() {
      tearDownMocksForPromotionManager()
      unmockkObject(AniMemePromotionService)
      unmockkObject(RestClient)
      unmockkObject(PluginService)
      unmockkObject(AppService)
      removeStuff()
    }

    private fun removeStuff() {
      Files.walk(TestTools.getTestAssetPath(testDirectory))
        .filter { it.isFile() }
        .forEach { Files.deleteIfExists(it) }
    }
  }

  @Before
  fun cleanUp() {
    clearMocks(AniMemePromotionService)
    every { AppService.getApplicationName() } returns "零二"
    removeStuff()
  }

  @Test
  fun `should write new version`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val beforePromotion = Instant.now()

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger.allowedToPromote).isTrue
    assertThat(postLedger.user).isNotNull
    assertThat(postLedger.seenPromotions.isEmpty()).isTrue
    assertThat(postLedger.versionInstallDates.size).isEqualTo(1)
    assertThat(postLedger.versionInstallDates["Ryuko"]).isBetween(
      beforePromotion,
      Instant.now()
    )

    validateLedgerCallback(postLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should always write new version`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val beforeRyuko = Instant.now()

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postRyukoLedger = LedgerMaster.readLedger()

    assertThat(postRyukoLedger.allowedToPromote).isTrue
    assertThat(postRyukoLedger.user).isNotNull
    assertThat(postRyukoLedger.seenPromotions.isEmpty()).isTrue
    assertThat(postRyukoLedger.versionInstallDates.size).isEqualTo(1)
    assertThat(postRyukoLedger.versionInstallDates["Ryuko"]).isBetween(
      beforeRyuko,
      Instant.now()
    )

    val beforeRin = Instant.now()

    promotionManager.registerPromotion("Rin", true)

    val postRinLedger = LedgerMaster.readLedger()

    assertThat(postRinLedger.allowedToPromote).isTrue
    assertThat(postRinLedger.user).isNotNull
    assertThat(postRinLedger.seenPromotions.isEmpty()).isTrue
    assertThat(postRinLedger.versionInstallDates.size).isEqualTo(2)
    assertThat(postRyukoLedger.versionInstallDates["Ryuko"]).isBetween(
      beforeRyuko,
      Instant.now()
    )
    assertThat(postRinLedger.versionInstallDates["Rin"]).isBetween(
      beforeRin,
      Instant.now()
    )

    validateLedgerCallback(postRinLedger, beforeRin)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should not do anything when AMII is installed`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()

    every { PluginService.isRiderExtensionInstalled() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }
  }

  @Test
  fun `should not do anything when has been promoted before`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(
        ANI_MEME_PROMOTION_ID to Promotion(
          ANI_MEME_PROMOTION_ID,
          Instant.now(),
          PromotionStatus.REJECTED
        )
      ),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }
  }

  @Test
  fun `should not do anything when not owner of lock`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false

    assertThat(LockMaster.acquireLock("Misato")).isTrue

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }
  }

  @Test
  fun `should not promote when not allowed`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      false
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should not promote when previous promotion was rejected`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(
        ANI_MEME_PROMOTION_ID to Promotion(ANI_MEME_PROMOTION_ID, Instant.now(), PromotionStatus.REJECTED)
      ),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should promote when previous promotion was accepted`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(
        ANI_MEME_PROMOTION_ID to Promotion(ANI_MEME_PROMOTION_ID, Instant.now(), PromotionStatus.ACCEPTED)
      ),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val beforePromotion = Instant.now()
    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    validateLedgerCallback(currentLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should not promote when rider extension is not compatible`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns false
    every { AppService.isRiderPlatform() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(
        ANI_MEME_PROMOTION_ID to Promotion(ANI_MEME_PROMOTION_ID, Instant.now(), PromotionStatus.ACCEPTED)
      ),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should promote when previous promotion was not shown`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(
        ANI_MEME_PROMOTION_ID to Promotion(ANI_MEME_PROMOTION_ID, Instant.now(), PromotionStatus.ACCEPTED)
      ),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val beforePromotion = Instant.now()
    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    val promotionSlot = slot<(PromotionResults) -> Unit>()
    val definitionSlot = slot<PromotionDefinition>()
    val rejectionSlot = slot<() -> Unit>()
    val newUserSlot = slot<Boolean>()
    verify {
      AniMemePromotionService.runPromotion(
        capture(newUserSlot),
        capture(definitionSlot),
        capture(promotionSlot),
        capture(rejectionSlot)
      )
    }

    rejectionSlot.captured()

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
    LockMaster.releaseLock("Syrena")

    validateLedgerCallback(currentLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should promote when not locked`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val beforePromotion = Instant.now()
    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    validateLedgerCallback(currentLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should not promote when not rider`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { AppService.isRiderPlatform() } returns false

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    verify { AniMemePromotionService wasNot Called }
  }

  @Test
  fun `should promote when primary assets are down`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val beforePromotion = Instant.now()
    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    validateLedgerCallback(currentLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  @Test
  fun `should break old lock`() {
    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
    every { PluginService.isRiderExtensionInstalled() } returns false
    every { PluginService.canRiderExtensionBeInstalled() } returns true
    every { AppService.isRiderPlatform() } returns true

    LockMaster.writeLock(
      Lock(
        "Misato",
        Instant.now().minusMillis(
          TimeUnit.MILLISECONDS.convert(3, TimeUnit.HOURS)
        )
      )
    )

    val currentLedger = PromotionLedger(
      UUID.randomUUID(),
      mutableMapOf("Ryuko" to Instant.now().minus(Period.ofDays(3))),
      mutableMapOf(),
      true
    )

    LedgerMaster.persistLedger(currentLedger)

    val beforePromotion = Instant.now()
    val promotionManager = PromotionManagerImpl()
    promotionManager.registerPromotion("Ryuko", true)

    val postLedger = LedgerMaster.readLedger()

    assertThat(postLedger).isEqualTo(currentLedger)

    validateLedgerCallback(currentLedger, beforePromotion)

    assertThat(LockMaster.acquireLock("Syrena")).isTrue
  }

  private fun validateLedgerCallback(
    currentLedger: PromotionLedger,
    beforePromotion: Instant?
  ) {
    val promotionSlot = slot<(PromotionResults) -> Unit>()
    val definitionSlot = slot<PromotionDefinition>()
    val rejectionSlot = slot<() -> Unit>()
    val newUserSlot = slot<Boolean>()
    verify {
      AniMemePromotionService.runPromotion(
        capture(newUserSlot),
        capture(definitionSlot),
        capture(promotionSlot),
        capture(rejectionSlot)
      )
    }

    val promotionCallback = promotionSlot.captured
    promotionCallback(PromotionResults(PromotionStatus.BLOCKED))

    val postBlockedTime = Instant.now()
    val postBlocked = LedgerMaster.readLedger()
    assertThat(postBlocked.user).isEqualTo(currentLedger.user)
    assertThat(postBlocked.versionInstallDates).isEqualTo(currentLedger.versionInstallDates)
    assertThat(postBlocked.allowedToPromote).isFalse
    assertThat(postBlocked.seenPromotions[ANI_MEME_PROMOTION_ID]?.result).isEqualTo(PromotionStatus.BLOCKED)
    assertThat(postBlocked.seenPromotions[ANI_MEME_PROMOTION_ID]?.id).isEqualTo(ANI_MEME_PROMOTION_ID)
    assertThat(postBlocked.seenPromotions[ANI_MEME_PROMOTION_ID]?.datePromoted).isBetween(
      beforePromotion,
      postBlockedTime
    )

    promotionCallback(PromotionResults(PromotionStatus.REJECTED))

    val postRejectedTime = Instant.now()
    val postRejected = LedgerMaster.readLedger()
    assertThat(postRejected.user).isEqualTo(currentLedger.user)
    assertThat(postRejected.versionInstallDates).isEqualTo(currentLedger.versionInstallDates)
    assertThat(postRejected.allowedToPromote).isTrue
    assertThat(postRejected.seenPromotions[ANI_MEME_PROMOTION_ID]?.result).isEqualTo(PromotionStatus.REJECTED)
    assertThat(postRejected.seenPromotions[ANI_MEME_PROMOTION_ID]?.id).isEqualTo(ANI_MEME_PROMOTION_ID)
    assertThat(postRejected.seenPromotions[ANI_MEME_PROMOTION_ID]?.datePromoted).isBetween(
      postBlockedTime,
      postRejectedTime
    )

    promotionCallback(PromotionResults(PromotionStatus.ACCEPTED))

    val postAcceptedTime = Instant.now()
    val postAccepted = LedgerMaster.readLedger()
    assertThat(postAccepted.user).isEqualTo(currentLedger.user)
    assertThat(postAccepted.versionInstallDates).isEqualTo(currentLedger.versionInstallDates)
    assertThat(postAccepted.allowedToPromote).isTrue
    assertThat(postAccepted.seenPromotions[ANI_MEME_PROMOTION_ID]?.result).isEqualTo(PromotionStatus.ACCEPTED)
    assertThat(postAccepted.seenPromotions[ANI_MEME_PROMOTION_ID]?.id).isEqualTo(ANI_MEME_PROMOTION_ID)
    assertThat(postAccepted.seenPromotions[ANI_MEME_PROMOTION_ID]?.datePromoted).isBetween(
      postRejectedTime,
      postAcceptedTime
    )
  }
}
