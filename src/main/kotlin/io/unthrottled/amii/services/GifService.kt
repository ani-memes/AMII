package io.unthrottled.amii.services

import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import java.awt.Dimension
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageInputStream

object GifService : Logging {
  private val cache = ConcurrentHashMap<URI, Int>()
  private val dimensionCache = ConcurrentHashMap<URI, Dimension>()

  fun getDuration(filePath: URI): Int =
    getCachedItem(cache, filePath) {
      fetchGifDuration(it)
    }

  fun getDimensions(filePath: URI): Dimension =
    getCachedItem(dimensionCache, filePath) {
      fetchImageDimensions(it)
    }

  private fun <R> getCachedItem(
    cacheGuy: ConcurrentHashMap<URI, R>,
    filePath: URI,
    cacheGetter: (URI) -> R
  ): R {
    if (cacheGuy.containsKey(filePath).not()) {
      cacheGuy[filePath] = cacheGetter(filePath)
    }

    return cacheGuy[filePath]!!
  }

  private fun fetchGifDuration(filePath: URI) = runSafelyWithResult({
    createImageStream(filePath)
      .use { imageInputStream ->
        val reader = getImageReader(imageInputStream)
        val numImages = reader.getNumImages(true)
        val gifCycleDuration = (0 until numImages)
          .mapNotNull { reader.getImageMetadata(it) }
          .sumOf { getFrameDelay(it) }
        gifCycleDuration
      }
  }) {
    logger().warn("Unable to read image count", it)
    -1
  }

  private fun fetchImageDimensions(filePath: URI): Dimension =
    runSafelyWithResult({
      createImageStream(filePath)
        .use { imageInputStream ->
          val reader = getImageReader(imageInputStream)
          Dimension(
            reader.getWidth(0),
            reader.getHeight(0)
          )
        }
    }) {
      logger().warn("Unable to read image dimensions", it)
      Dimension(-1, -1)
    }

  private fun createImageStream(filePath: URI) =
    ImageIO.createImageInputStream(
      Paths.get(filePath).toFile()
    )

  private fun getImageReader(imageInputStream: ImageInputStream): ImageReader {
    val reader = ImageIO.getImageReadersBySuffix("gif").next()
    reader.setInput(imageInputStream, false)
    return reader
  }

  private fun getFrameDelay(imageMetadata: IIOMetadata): Int {
    val rootNode = imageMetadata.getAsTree(
      imageMetadata.nativeMetadataFormatName
    )
    if (rootNode !is IIOMetadataNode) return 0

    return getDelayTime(
      (0..rootNode.length)
        .map { rootNode.item(it) }
        .filterIsInstance(IIOMetadataNode::class.java)
        .find { it.nodeName.equals("GraphicControlExtension", ignoreCase = true) }
    ) ?: getDelayTime(rootNode) ?: 0
  }

  private const val TENTH_OF_SECOND_TO_MILLS = 10
  private fun getDelayTime(rootNode: IIOMetadataNode?): Int? =
    rootNode?.getAttribute("delayTime")?.toInt()?.times(TENTH_OF_SECOND_TO_MILLS)
}
