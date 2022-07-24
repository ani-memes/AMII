package io.unthrottled.amii.config.ui

import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import io.unthrottled.amii.assets.MemeAssetCategory

class MemeCategoryPopupStep(
  @NlsContexts.PopupTitle title: String,
  available: List<MemeAssetCategory>,
  private val onResult: (MemeAssetCategory) -> Unit
) :
  BaseListPopupStep<MemeAssetCategory>(title, available ) {

  override fun getSeparatorAbove(value: MemeAssetCategory) = null

  @NlsSafe
  override fun getTextFor(value: MemeAssetCategory) = value.name

  override fun onChosen(selectedValue: MemeAssetCategory, finalChoice: Boolean): PopupStep<*>? {
    return doFinalStep { onResult(selectedValue) }
  }
}
