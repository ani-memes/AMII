package io.unthrottled.amii.config.ui;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.VisualAssetEntity;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.tools.AssetTools;
import io.unthrottled.doki.settings.CustomStickerChooser;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class CustomMemeList {
  private ConfigSettingsModel pluginSettingsModel;
  private JButton chooseDirectoryButton;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;

  public CustomMemeList(
    Consumer<MemeAsset> onTest,
    ConfigSettingsModel pluginSettingsModel
  ) {
    this.pluginSettingsModel = pluginSettingsModel;
    ayyLmao.setLayout(new BoxLayout(ayyLmao, BoxLayout.PAGE_AXIS));
    try {
      Files.walk(
        Paths.get("/Users/alexsimons/Downloads/customAssets")
      )
        .filter(Files::isReadable)
        .filter(Files::isRegularFile)
        .forEach(path -> {
          String id = AssetTools.calculateMD5Hash(path);
          CustomMemePanel customMemePanel = new CustomMemePanel(
            onTest,
            new VisualAssetEntity(
              id,
              path.toUri().toString(),
              "",
              Collections.emptySet(),
              Collections.emptyList(),
              new VisualAssetRepresentation(
                id,
                path.toUri().toString(),
                "",
                Collections.emptyList(),
                Collections.emptyList(),
                "",
                false
              ),
              null
            )
          );
          ayyLmao.add(customMemePanel.getComponent());
        });
    } catch (IOException e) {
      e.printStackTrace();
    }

    chooseDirectoryButton.addActionListener(e -> {
      CustomStickerChooser dialog = new CustomStickerChooser(
        Arrays.stream(ProjectManager.getInstance().getOpenProjects()).findFirst().orElse(
          ProjectManager.getInstance().getDefaultProject()
        ),
        this.pluginSettingsModel.getCustomAssetsPath()
      );

      dialog.showAndGet();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        String path = dialog.getPath();
        this.pluginSettingsModel.setCustomAssetsPath(path);
      }
    });
  }

  public void setPluginSettingsModel(ConfigSettingsModel pluginSettingsModel) {
    this.pluginSettingsModel = pluginSettingsModel;
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
