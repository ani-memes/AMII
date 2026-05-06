package io.unthrottled.amii.assets

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.unthrottled.amii.integrations.RestTools
import io.unthrottled.amii.tools.TestTools
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.nio.file.Files
import java.util.Comparator
import java.util.Optional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ContentAssetManagerRegressionTest {

  private val testDirectory = TestTools.getTestAssetPath("content-assets-regression")

  @Before
  fun setUp() {
    mockkObject(LocalStorageService)
    mockkObject(RestTools)
    every { LocalStorageService.getContentDirectory() } returns testDirectory.toString()
    every { LocalStorageService.createDirectories(any()) } answers {
      Files.createDirectories(firstArg<java.nio.file.Path>().parent)
    }
    ContentAssetManager.isDispatchThread = { true }
    ContentAssetManager.executeInBackground = { runnable ->
      Thread(runnable, "asset-resolution-test").start()
    }
  }

  @After
  fun tearDown() {
    ContentAssetManager.isDispatchThread = {
      com.intellij.openapi.application.ApplicationManager.getApplication()?.isDispatchThread == true
    }
    ContentAssetManager.executeInBackground = { runnable ->
      com.intellij.openapi.application.ApplicationManager.getApplication()?.executeOnPooledThread(runnable) ?: runnable()
    }
    unmockkObject(LocalStorageService)
    unmockkObject(RestTools)
    if (Files.exists(testDirectory)) {
      Files.walk(testDirectory)
        .sorted(Comparator.reverseOrder())
        .forEach { Files.deleteIfExists(it) }
    }
  }

  @Test
  fun `resolveAssetUrl does not perform network synchronously on EDT`() {
    val requestStarted = CountDownLatch(1)
    every { RestTools.performRequest<URI>(any(), any()) } answers {
      requestStarted.countDown()
      Optional.empty()
    }

    val result = ContentAssetManager.resolveAssetUrl(AssetCategory.PROMOTION, "missing/logo.png")

    assertThat(result).isNotNull
    assertThat(result).isEmpty
    assertThat(requestStarted.await(5, TimeUnit.SECONDS)).isTrue
  }
}
