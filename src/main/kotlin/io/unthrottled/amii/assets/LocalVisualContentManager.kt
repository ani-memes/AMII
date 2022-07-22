package io.unthrottled.amii.assets

import io.unthrottled.amii.config.Config
import io.unthrottled.amii.tools.AssetTools.calculateMD5Hash
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

object LocalVisualContentManager : Logging {

  fun supplyAllVisualAssetDefinitions(): Set<VisualAssetRepresentation> {
    return readLocalAssetDirectory(Config.instance.customAssetsPath)
  }

  @JvmStatic
  fun supplyAllVisualAssetDefinitionsFromWorkingDirectory(
    workingDirectory: String
  ): Set<VisualAssetRepresentation> {
    return readLocalAssetDirectory(workingDirectory)
  }

  private fun readLocalAssetDirectory(workingDirectory: String): Set<VisualAssetRepresentation> {
    if (workingDirectory.isEmpty() ||
      Files.exists(Paths.get(workingDirectory)).not()
    ) {
      return emptySet()
    }

    return runSafelyWithResult({
      Files.walk(
        Paths.get(workingDirectory)
      )
        .filter { path: Path ->
          Files.isReadable(
            path
          )
        }
        .filter { path: Path ->
          Files.isRegularFile(
            path
          )
        }
        .map { path ->
          val id = calculateMD5Hash(path)
          VisualAssetRepresentation(
            id,
            path.toUri().toString(),
            "", emptyList(), emptyList(),
            "",
            false
          )
        }
        .collect(Collectors.toSet())
    }) {
      this.logger().warn("Unable to walk custom working directory for raisins.", it)
      emptySet()
    }
  }
}
