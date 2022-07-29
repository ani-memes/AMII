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

@Suppress("TooManyFunctions", "LongMethod") // cuz I said so
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
    cachedAssets = readLocalDirectoryWithAutoTag(
      AssetFetchOptions()
    )
  }

  private var ledger = LocalVisualAssetStorageService.getInitialItem()

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
    assetFetchOptions: AssetFetchOptions
  ): Set<VisualAssetRepresentation> {
    val readLocalDirectoryWithAutoTag = readLocalDirectoryWithAutoTag(assetFetchOptions)
    cachedAssets = readLocalDirectoryWithAutoTag
    return readLocalDirectoryWithAutoTag
  }

  fun createAutoTagDirectories(workingDirectory: String) {
    getAutoTagDirectories(workingDirectory)
      .map { it.path }
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

  private fun getAutoTagDirectories(customAssetsPath: String): Stream<AutoTagDirectory> {
    return Arrays.stream(MemeAssetCategory.values())
      .flatMap { cat: MemeAssetCategory ->
        Stream.of(
          AutoTagDirectory(
            Paths.get(
              customAssetsPath, cat.name.lowercase()
            ),
            cat,
            false,
          ),
          AutoTagDirectory(
            Paths.get(
              customAssetsPath, "suggestive", cat.name.lowercase()
            ),
            cat,
            true
          )
        )
      }
  }

  private fun autoTagAssets(workingDirectory: String) {
    if (Config.instance.createAutoTagDirectories.not()) {
      logger().info("Not tagging items because auto tagging is not enabled.")
      return
    }

    assertNotAWTThread()

    createAutoTagDirectories(workingDirectory)

    val allLocalAssets = associateAllAssets(workingDirectory)

    val partitionedAutoTagAssets:
      Map<Boolean, List<VisualAssetRepresentation>> = getAutoTagDirectories(workingDirectory)
      .flatMap { autoTagDir ->
        try {
          walkDirectoryForAssets(
            autoTagDir.path.toString()
          )
            .map { assetPath: Path? ->
              // todo: probably shouldn't calculate md5 hash.
              allLocalAssets[
                calculateMD5Hash(
                  assetPath!!
                )
              ]
            }
            .filter { obj: VisualAssetRepresentation? ->
              Objects.nonNull(
                obj
              )
            }
            .map { it!! }
            .map { rep ->
              var modified = false
              val memeAssetCategoryValue = autoTagDir.category.value
              var usableRep = rep
              if (!usableRep.cat.contains(memeAssetCategoryValue)) {
                usableRep.cat.add(memeAssetCategoryValue)
                modified = true
              }

              if (autoTagDir.isLewd && usableRep.lewd?.not() == true) {
                usableRep = usableRep.copy(lewd = true)
              }

              usableRep to modified
            }
        } catch (e: RuntimeException) {
          logger().warn("Unable to auto tag assets for dir ${autoTagDir.path}", e)
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

  private fun associateAllAssets(
    workingDirectory: String
  ): MutableMap<String, VisualAssetRepresentation> =
    readDirectory(
      AssetFetchOptions(
        workingDirectory,
        includeLewds = true,
      )
    )
      .stream()
      .collect(
        Collectors.toMap(
          VisualAssetRepresentation::id,
          Function.identity()
        ) { a, _ -> a }
      )

  private fun readLocalDirectoryWithAutoTag(assetFetchOptions: AssetFetchOptions): Set<VisualAssetRepresentation> {
    val workingDirectory = assetFetchOptions.workingDirectory
    if (workingDirectory.isEmpty() ||
      Files.exists(Paths.get(workingDirectory)).not()
    ) {
      return emptySet()
    }

    assertNotAWTThread()

    autoTagAssets(workingDirectory)

    return readDirectory(assetFetchOptions)
  }

  private fun readDirectory(assetFetchOptions: AssetFetchOptions): Set<VisualAssetRepresentation> {
    val workingDirectory = assetFetchOptions.workingDirectory
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
        .filter { rep ->
          rep.lewd != true ||
            assetFetchOptions.includeLewds
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
    ApplicationManager.getApplication().executeOnPooledThread {
      rescanDirectory()
    }
  }

  fun init() {
    // to warm up
  }
}

data class AutoTagDirectory(
  val path: Path,
  val category: MemeAssetCategory,
  val isLewd: Boolean,
)

data class AssetFetchOptions(
  val workingDirectory: String = Config.instance.customAssetsPath,
  val includeLewds: Boolean = Config.instance.allowLewds
)
