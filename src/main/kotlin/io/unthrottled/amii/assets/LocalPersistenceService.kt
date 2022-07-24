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

abstract class LocalPersistenceService<T>(
  private val fileName: String,
  private val classType: Class<T>,
) {
  private val log = Logger.getInstance(this::class.java)

  private val gson = GsonBuilder()
    .create()

  private val ledgerPath = LocalStorageService.constructLocalContentPath(
    AssetCategory.META,
    fileName,
  )

  fun getInitialItem(): T =
    if (ledgerPath.exists()) {
      readLedger()
    } else {
      buildDefaultLedger()
    }

  protected fun readLedger(): T =
    runSafelyWithResult({
      Files.newInputStream(ledgerPath)
        .use {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            classType
          )
        }
    }) {
      log.warn("Unable to read promotion ledger: $fileName, for raisins.", it)
      buildDefaultLedger()
    }.toOptional()
      .map { item ->
        decorateItem(item)
      }
      .orElseGet {
        buildDefaultLedger()
      }

  protected abstract fun decorateItem(item: T): T

  protected abstract fun buildDefaultLedger(): T

  fun persistLedger(themeObservationLedger: T): T {
    if (ledgerPath.exists().not()) {
      LocalStorageService.createDirectories(ledgerPath)
    }

    return runSafelyWithResult({
      Files.newBufferedWriter(
        ledgerPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use {
        val mostCurrentLedger = combineWithOnDisk(themeObservationLedger)
        it.write(
          gson.toJson(mostCurrentLedger)
        )
        mostCurrentLedger
      }
    }) {
      log.warn("Unable to persist ledger: $fileName, for raisins", it)
      themeObservationLedger
    }
  }

  abstract fun combineWithOnDisk(themeObservationLedger: T): T
}
