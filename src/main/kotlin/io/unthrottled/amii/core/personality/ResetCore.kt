package io.unthrottled.amii.core.personality

import io.unthrottled.amii.assets.MemeAssetCategory
import io.unthrottled.amii.core.personality.emotions.EmotionalMutationAction
import io.unthrottled.amii.events.UserEvent
import io.unthrottled.amii.events.UserEventCategory
import io.unthrottled.amii.events.UserEvents
import io.unthrottled.amii.memes.memeEventService
import io.unthrottled.amii.tools.PluginMessageBundle

class ResetCore {

  fun processMutationEvent(emotionalMutationAction: EmotionalMutationAction) {
    emotionalMutationAction.project?.memeEventService()?.createAndDisplayMemeEventFromCategories(
      UserEvent(
        UserEvents.RELAX,
        UserEventCategory.NEUTRAL,
        PluginMessageBundle.message("user.event.relax.name"),
        emotionalMutationAction.project
      ),
      MemeAssetCategory.ACKNOWLEDGEMENT,
      MemeAssetCategory.ACKNOWLEDGEMENT,
      MemeAssetCategory.ACKNOWLEDGEMENT,
      MemeAssetCategory.HAPPY,
    )
  }
}
