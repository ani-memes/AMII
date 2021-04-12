package io.unthrottled.amii.tools

import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.unthrottled.amii.assets.LocalStorageService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object TestTools {

  fun getTestAssetPath(vararg extraDirectories: String): Path {
    val testAssetDirectory = Paths.get(".", "testAssets", *extraDirectories)
      .normalize()
      .toAbsolutePath()
    if (Files.exists(testAssetDirectory).not()) {
      Files.createDirectories(testAssetDirectory)
    }
    return testAssetDirectory
  }

  fun setUpMocksForManager() {
    mockkObject(LocalStorageService)
  }

  fun tearDownMocksForPromotionManager() {
    unmockkObject(LocalStorageService)
  }
}
