package io.unthrottled.amii.assets

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import com.intellij.util.io.isFile
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.unthrottled.amii.assets.ContentAssetManager.ASSET_SOURCE
import io.unthrottled.amii.integrations.HttpClientFactory
import io.unthrottled.amii.integrations.RestTools
import io.unthrottled.amii.test.tools.TestTools
import io.unthrottled.amii.test.tools.TestTools.setUpMocksForManager
import io.unthrottled.amii.test.tools.TestTools.tearDownMocksForPromotionManager
import io.unthrottled.amii.tools.readAllTheBytes
import io.unthrottled.amii.tools.toOptional
import java.io.InputStream
import java.nio.file.Files
import org.assertj.core.api.Assertions
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class VisualContentManagerIntegrationTest : LightPlatformCodeInsightFixture4TestCase() {

  companion object {
    private const val testDirectory = "apiAssets"
    private val initialAPIResponse = javaClass.classLoader
      .getResourceAsStream("initial-visuals-api-response.json")
      .use {
        it.readAllTheBytes()
          .decodeToString()
      }
    private val updatedAPIResponse = javaClass.classLoader
      .getResourceAsStream("updated-visuals-api-response.json")
      .use {
        it.readAllTheBytes()
          .decodeToString()
      }

    @JvmStatic
    @BeforeClass
    fun setUp_() {
      setUpMocksForManager()
      mockkObject(RestTools)
      val captureSlot = slot<(InputStream) -> String>()
      every {
        RestTools.performRequest(
          "$ASSET_SOURCE/public/assets/visuals",
          capture(captureSlot)
        )
      } returns initialAPIResponse.toOptional()

    }

    @JvmStatic
    @AfterClass
    fun tearDown_() {
      tearDownMocksForPromotionManager()
      unmockkObject(RestTools)
    }
  }

  @Before
  fun cleanUp() {
    Files.walk(TestTools.getTestAssetPath(testDirectory))
      .filter { it.isFile() }
      .forEach { Files.deleteIfExists(it) }

    every { LocalStorageService.getContentDirectory() } returns
      TestTools.getTestAssetPath(testDirectory).toString()
  }


  @Test
  fun `should initially request full asset list`() {
    val allAssets = VisualContentManager.supplyAllAssetDefinitions()

    Assertions.assertThat(allAssets).isNotEmpty
  }
}
