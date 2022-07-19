package io.unthrottled.amii.config.ui;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.doki.settings.CustomStickerChooser;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.Arrays;
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
    CustomMemePanel customMemePanel = new CustomMemePanel(
      onTest
    );
    ayyLmao.add(customMemePanel.getComponent(), new GridConstraints());

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
