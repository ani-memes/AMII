package io.unthrottled.amii.memes

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import io.unthrottled.amii.tools.BalloonTools.getIDEFrame
import io.unthrottled.amii.tools.toOptional
import java.util.Optional

object MemeFactory {

  @Suppress("MaxLineLength")
  fun createMemeDisplay(project: Project): Optional<MemePanel> =
    UIUtil.getRootPane(
      getIDEFrame(project).component
    )?.layeredPane
      .toOptional()
      .map {
        MemePanel(
          it,
          """<html>
<div style='margin: 5;'>
<img
  alt='das image'
  src='file:///home/alex/workspace/waifuMotivator/waifu-motivator-plugin/build/idea-sandbox/config/waifuMotivationAssets/visuals/smug/smugumin_1.gif'/>
</div>
</html>
      """
        )
      }
}
