package io.unthrottled.amii.assets

import io.unthrottled.amii.tools.TestTools
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import java.util.Comparator
import java.nio.file.Files
import kotlin.io.path.isRegularFile

class LocalVisualContentManagerRegressionTest {

  private val testDirectory = TestTools.getTestAssetPath("custom-assets-regression")

  @After
  fun cleanUp() {
    if (Files.exists(testDirectory)) {
      Files.walk(testDirectory)
        .sorted(Comparator.reverseOrder())
        .forEach { Files.deleteIfExists(it) }
    }
  }

  @Test
  fun `walkDirectoryForAssets returns no items for empty directory`() {
    val assets = LocalVisualAssetScanner.withAssetsInDirectory(testDirectory.toString()) {
      it.toList()
    }

    assertThat(assets).isEmpty()
  }

  @Test
  fun `walkDirectoryForAssets returns only gif files`() {
    Files.writeString(testDirectory.resolve("one.gif"), "not really a gif")
    Files.writeString(testDirectory.resolve("two.txt"), "not a gif")

    val assets = LocalVisualAssetScanner.withAssetsInDirectory(testDirectory.toString()) {
      it.map { path -> path.fileName.toString() }.toList()
    }

    assertThat(assets).containsExactly("one.gif")
  }

  @Test
  fun `walkDirectoryForAssets handles many gif files without creating UI panels`() {
    repeat(600) { idx ->
      Files.writeString(testDirectory.resolve("asset-$idx.gif"), "not really a gif")
    }

    val assetCount = LocalVisualAssetScanner.withAssetsInDirectory(testDirectory.toString()) {
      it.filter { path -> path.isRegularFile() }.count()
    }

    assertThat(assetCount).isEqualTo(600)
  }
}
