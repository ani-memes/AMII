package io.unthrottled.amii.assets

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.exists
import io.unthrottled.amii.tools.runSafely
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class AssetObservationLedger(
  val assetSeenCounts: ConcurrentHashMap<String, Int>,
  val writeDate: Instant,
)

object AssetObservationService {
  private val log = Logger.getInstance(AssetObservationService::class.java)

  private const val MAX_ALLOWED_DAYS_PERSISTED = 7L

  private val gson = GsonBuilder()
    .create()

  private val ledgerPath = ContentAssetManager.constructLocalContentPath(
    AssetCategory.META,
    "seen-assets-ledger.json"
  )

  fun getInitialLedger(): AssetObservationLedger =
    if (ledgerPath.exists()) {
      readLedger()
    } else {
      buildDefaultLedger()
    }

  private fun readLedger(): AssetObservationLedger =
    runSafelyWithResult({
      Files.newInputStream(ledgerPath)
        .use {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            AssetObservationLedger::class.java
          )
        }
    }) {
      log.warn("Unable to read promotion ledger for raisins.", it)
      buildDefaultLedger()
    }.toOptional()
      .filter { Duration.between(it.writeDate, Instant.now()).toDays() <= MAX_ALLOWED_DAYS_PERSISTED }
      .orElseGet {
        buildDefaultLedger()
      }

  private fun buildDefaultLedger() = AssetObservationLedger(ConcurrentHashMap(), Instant.now())

  fun persistLedger(assetObservationLedger: AssetObservationLedger) {
    if (ledgerPath.exists().not()) {
      LocalStorageService.createDirectories(ledgerPath)
    }

    runSafely({
      Files.newBufferedWriter(
        ledgerPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use {
        it.write(
          gson.toJson(assetObservationLedger)
        )
      }
    }) {
      log.warn("Unable to persist ledger for raisins", it)
    }
  }
}
