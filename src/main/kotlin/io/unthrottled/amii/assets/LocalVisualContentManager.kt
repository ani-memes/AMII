package io.unthrottled.amii.assets

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.amii.assets.VisualEntityRepository.Companion.instance
import io.unthrottled.amii.config.Config
import io.unthrottled.amii.config.ConfigListener
import io.unthrottled.amii.tools.AssetTools.calculateMD5Hash
import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.assertNotAWTThread
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafely
import io.unthrottled.amii.tools.runSafelyWithResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays
import java.util.Objects
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

object LocalVisualContentManager : Logging, Disposable, ConfigListener {

  private val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

  init {
    messageBusConnection.subscribe(ConfigListener.CONFIG_TOPIC, this)
    ApplicationManager.getApplication().executeOnPooledThread {
      rescanDirectory()
    }
  }

  private var cachedAssets: Set<VisualAssetRepresentation> = emptySet()
  fun supplyAllExistingVisualAssetRepresentations(): Set<VisualAssetRepresentation> {
    return cachedAssets
  }

  fun rescanDirectory() {
    cachedAssets = readLocalDirectoryWithAutoTag(Config.instance.customAssetsPath)
  }

  private var ledger = LocalVisualAssetStorageService.getInitialItem()
  private var usableAssets = buildUsableAssetList() // todo: use this

  private fun buildUsableAssetList(): Set<VisualAssetRepresentation> {
    return ledger.savedVisualAssets.values
      .filter { rep ->
        rep.lewd != true ||
          Config.instance.allowLewds
      }
      .toSet()
  }

  fun supplyAllUserModifiedVisualRepresentations(): Set<VisualAssetRepresentation> {
    return ledger.savedVisualAssets.values.toSet()
  }

  fun updateRepresentation(visualAssetRepresentation: VisualAssetRepresentation) {
    val newMap = ledger.savedVisualAssets.toMutableMap()
    newMap[visualAssetRepresentation.id] = visualAssetRepresentation
    ledger = ledger.copy(savedVisualAssets = newMap)
    LocalVisualAssetStorageService.persistLedger(ledger)
  }

  fun updateRepresentations(visualAssetRepresentations: List<VisualAssetRepresentation>) {
    val newMap = ledger.savedVisualAssets.toMutableMap()
    visualAssetRepresentations.forEach { visualAssetRepresentation ->
      newMap[visualAssetRepresentation.id] = visualAssetRepresentation
    }
    ledger = ledger.copy(savedVisualAssets = newMap)
    LocalVisualAssetStorageService.persistLedger(ledger)
  }

  @JvmStatic
  fun supplyAllVisualAssetDefinitionsFromWorkingDirectory(
    workingDirectory: String
  ): Set<VisualAssetRepresentation> {
    return readLocalDirectoryWithAutoTag(workingDirectory)
  }

  fun createAutoTagDirectories(workingDirectory: String) {
    getAutoTagDirectories(workingDirectory)
      .map { it.first }
      .filter { autoTagDirectory ->
        !Files.exists(
          autoTagDirectory
        )
      }.forEach { autoTagDirectory ->
        runSafely({
          Files.createDirectories(autoTagDirectory)
        }) {
          logger().warn("Unable to create auto tag dir $autoTagDirectory", it)
        }
      }
  }

  private fun getAutoTagDirectories(customAssetsPath: String): Stream<Pair<Path, MemeAssetCategory>> {
    return Arrays.stream(MemeAssetCategory.values())
      .map { cat: MemeAssetCategory ->
        Pair(
          Paths.get(
            customAssetsPath, cat.name.lowercase()
          ),
          cat
        )
      }
  }

  fun autoTagAssets(workingDirectory: String) {
    if (Config.instance.createAutoTagDirectories.not()) {
      logger().info("Not tagging items because auto tagging is not enabled.")
      return
    }

    assertNotAWTThread()

    createAutoTagDirectories(workingDirectory)

    val allLocalAssets = readDirectory(workingDirectory)
      .stream()
      .collect(
        Collectors.toMap(
          VisualAssetRepresentation::id,
          Function.identity()
        ) { a, _ -> a })

    val partitionedAutoTagAssets:
      Map<Boolean, List<VisualAssetRepresentation>> = getAutoTagDirectories(workingDirectory)
      .flatMap { (first, memeAssetCategory): Pair<Path, MemeAssetCategory> ->
        try {
          walkDirectoryForAssets(
            first.toString()
          )
            .map { assetPath: Path? ->
              // todo: probably shouldn't calculate md5 hash.
              allLocalAssets[calculateMD5Hash(
                assetPath!!
              )]
            }
            .filter { obj: VisualAssetRepresentation? ->
              Objects.nonNull(
                obj
              )
            }
            .map { it!! }
            .map { rep ->
              val memeAssetCategoryValue = memeAssetCategory.value
              if (!rep.cat.contains(memeAssetCategoryValue)) {
                rep.cat.add(memeAssetCategoryValue)
                rep to true
              } else {
                rep to false
              }
            }
        } catch (e: RuntimeException) {
          logger().warn("Unable to auto tag assets for dir $first", e)
          Stream.empty<Pair<VisualAssetRepresentation, Boolean>>()
        }
      }.collect(
        Collectors.partitioningBy(
          { it.second },
          Collectors.mapping(
            { it.first },
            Collectors.toList()
          )
        )
      )
    val assetsToUpdate = partitionedAutoTagAssets[true] ?: emptyList()
    if (assetsToUpdate.isNotEmpty()) {
      updateRepresentations(
        assetsToUpdate
      )
      instance.refreshLocalAssets()
    }
  }

  private fun readLocalDirectoryWithAutoTag(workingDirectory: String): Set<VisualAssetRepresentation> {
    if (workingDirectory.isEmpty() ||
      Files.exists(Paths.get(workingDirectory)).not()
    ) {
      return emptySet()
    }

    assertNotAWTThread()

    autoTagAssets(workingDirectory)

    return readDirectory(workingDirectory)
  }

  private fun readDirectory(workingDirectory: String): Set<VisualAssetRepresentation> {
    return runSafelyWithResult({
      walkDirectoryForAssets(workingDirectory)
        .map { path ->
          val id = calculateMD5Hash(path)
          val savedAsset = ledger.savedVisualAssets[id]
          savedAsset?.duplicateWithNewPath(path.toUri().toString())
            ?: VisualAssetRepresentation(
              id,
              path.toUri().toString(),
              "", ArrayList(), ArrayList(),
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

  @JvmStatic
  fun walkDirectoryForAssets(workingDirectory: String): Stream<Path> =
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
      .filter { path ->
        path.fileName.toString().endsWith(".gif")
      }

  override fun dispose() {
    messageBusConnection.dispose()
  }

  override fun pluginConfigUpdated(config: Config) {
    rescanDirectory()
    usableAssets = buildUsableAssetList()
  }

  fun init() {
    // to warm up
  }
}
