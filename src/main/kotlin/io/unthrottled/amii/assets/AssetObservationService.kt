package io.unthrottled.amii.assets

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.exists
import io.unthrottled.amii.tools.runSafelyWithResult
import io.unthrottled.amii.tools.toOptional
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.max

data class AssetObservationLedger(
  val assetSeenCounts: ConcurrentMap<String, Int>,
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
      .map { observationLedger ->
        if (Duration.between(observationLedger.writeDate, Instant.now()).toDays() >= MAX_ALLOWED_DAYS_PERSISTED) {
          // reset counts so that way new assets that haven't been seen yet will eventually make
          // it to the ledger. Also enables users to see their favorite (most seen) assets again
          observationLedger.copy(
            assetSeenCounts = observationLedger.assetSeenCounts.entries.stream()
              .map { it.key to 1 }
              .collect(Collectors.toConcurrentMap({ it.first }, { it.second })
              { _, theChosenOne -> theChosenOne })
          )
        } else {
          observationLedger
        }
      }
      .orElseGet {
        buildDefaultLedger()
      }

  private fun buildDefaultLedger() = AssetObservationLedger(ConcurrentHashMap(), Instant.now())

  fun persistLedger(assetObservationLedger: AssetObservationLedger): AssetObservationLedger {
    if (ledgerPath.exists().not()) {
      LocalStorageService.createDirectories(ledgerPath)
    }

    return runSafelyWithResult({
      Files.newBufferedWriter(
        ledgerPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use {
        val mostCurrentLedger = combineWithOnDisk(assetObservationLedger)
        it.write(
          gson.toJson(mostCurrentLedger)
        )
        mostCurrentLedger
      }
    }) {
      log.warn("Unable to persist ledger for raisins", it)
      assetObservationLedger
    }
  }

  private fun combineWithOnDisk(assetObservationLedger: AssetObservationLedger): AssetObservationLedger {
    val onDisk = readLedger()
    return assetObservationLedger.copy(
      assetSeenCounts = Stream.concat(
        onDisk.assetSeenCounts.entries.stream(),
        assetObservationLedger.assetSeenCounts.entries.stream()
      ).collect(
        Collectors.toConcurrentMap(
          { it.key },
          { it.value },
          { ogAssetCount, thisIDEsCount ->
            // The user has many IDEs, so if one IDE is used more than the
            // other, (IDEs share the same asset source)
            // we want to remember that they've seen that asset a fair amount of times.
            max(ogAssetCount, thisIDEsCount)
          }
        ) { ConcurrentHashMap() }
      )
    )
  }
}
