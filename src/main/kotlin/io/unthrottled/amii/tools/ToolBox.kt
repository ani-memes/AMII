package io.unthrottled.amii.tools

import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.util.Optional
import java.util.stream.Stream

fun runSafely(runner: () -> Unit, onError: (Throwable) -> Unit): Unit =
  try {
    runner()
  } catch (e: Throwable) {
    onError(e)
  }

fun <T> runSafelyWithResult(runner: () -> T, onError: (Throwable) -> T): T =
  try {
    runner()
  } catch (e: Throwable) {
    onError(e)
  }

fun <T> T?.toOptional() = Optional.ofNullable(this)

fun <T> T?.toStream(): Stream<T> = Stream.of(this)

fun <T> Optional<T>.doOrElse(present: (T) -> Unit, notThere: () -> Unit) =
  this.map {
    it to true
  }.map {
    it.toOptional()
  }.orElseGet {
    (null to false).toOptional()
  }.ifPresent {
    if (it.second) {
      present(it.first)
    } else {
      notThere()
    }
  }

fun InputStream.readAllTheBytes(): ByteArray = IOUtils.toByteArray(this)

interface Logging

fun <T : Logging> T.logger(): Logger = Logger.getInstance(this::class.java)

inline fun <reified T> T.toArray(): Array<T> = arrayOf(this)
inline fun <reified T> T.toList(): List<T> = listOf(this)
