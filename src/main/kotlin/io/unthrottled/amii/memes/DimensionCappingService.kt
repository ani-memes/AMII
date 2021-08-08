package io.unthrottled.amii.memes

import io.unthrottled.amii.services.GifService
import java.net.URI

object DimensionCappingService {

  @JvmStatic
  fun getCappingStyle(maxHeight: Int, maxWidth: Int, filePath: URI): String {
    val setMaxHeight = maxHeight > 0
    val setMaxWidth = maxWidth > 0
    val memeDimensions = GifService.getDimensions(filePath)
    val memeHeight = memeDimensions.height
    val memeWidth = memeDimensions.width
    val heightIsGreaterThanOriginal = maxHeight < memeHeight
    val widthIsGreaterThanOriginal = maxWidth < memeWidth
    val needsToCap = heightIsGreaterThanOriginal || widthIsGreaterThanOriginal
    val canCap = setMaxHeight || setMaxWidth
    return if (needsToCap && canCap) {
      val heightIsGreater = memeHeight > memeWidth
      val (width, height) =
        when {
          heightIsGreaterThanOriginal &&
            heightIsGreater &&
            setMaxHeight ->
            (memeWidth / memeHeight.toDouble()) * maxHeight to maxHeight
          widthIsGreaterThanOriginal && setMaxWidth ->
            maxWidth to (memeHeight / memeWidth.toDouble()) * maxWidth
          else -> memeWidth to memeHeight
        }
      """height='${height.toInt()}' width='${width.toInt()}'"""
    } else {
      ""
    }
  }
}
