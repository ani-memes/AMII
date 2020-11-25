package io.unthrottled.amii.services

import io.unthrottled.amii.tools.Logging
import io.unthrottled.amii.tools.logger
import io.unthrottled.amii.tools.runSafelyWithResult
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode

object GifService : Logging {

  private val cache = ConcurrentHashMap<URI, Int>()

  fun getDuration(filePath: URI): Int {
    if (cache.containsKey(filePath).not()) {
      cache[filePath] = fetchGifDuration(filePath)
    }

    return cache[filePath]!!
  }

  private fun fetchGifDuration(filePath: URI) = runSafelyWithResult({
    ImageIO.createImageInputStream(
      Paths.get(filePath).toFile()
    ).use { imageInputStream ->
      val reader = ImageIO.getImageReadersBySuffix("gif").next()
      reader.setInput(imageInputStream, false)
      val numImages = reader.getNumImages(true)
      val gifCycleDuration = (0 until numImages)
        .mapNotNull { reader.getImageMetadata(it) }
        .map { getFrameDelay(it) }
        .sum()
      gifCycleDuration
    }
  }) {
    logger().warn("Unable to read image count", it)
    -1
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
