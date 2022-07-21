package io.unthrottled.amii.config.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.VisualAssetEntity;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.tools.AssetTools;
import io.unthrottled.doki.settings.CustomStickerChooser;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class CustomMemeList {
  private Consumer<MemeAsset> onTest;
  private ConfigSettingsModel pluginSettingsModel;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;
  private JPanel selectDir;

  public CustomMemeList(
    Consumer<MemeAsset> onTest,
    ConfigSettingsModel pluginSettingsModel
  ) {
    this.onTest = onTest;
    this.pluginSettingsModel = pluginSettingsModel;
    ayyLmao.setLayout(new BoxLayout(ayyLmao, BoxLayout.PAGE_AXIS));
  }

  private void populateDirectory(String workingDirectory) {
    if(workingDirectory.isBlank()) {
      return;
    }

    for (int i = 0; i < ayyLmao.getComponentCount(); i++) {
      try {
        ayyLmao.remove(0);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      Files.walk(
        Paths.get(workingDirectory)
      )
        .filter(Files::isReadable)
        .filter(Files::isRegularFile)
        .forEach(path -> {
          String id = AssetTools.calculateMD5Hash(path);
          CustomMemePanel customMemePanel = new CustomMemePanel(
            this.onTest,
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
  }

  public void setPluginSettingsModel(ConfigSettingsModel pluginSettingsModel) {
    this.pluginSettingsModel = pluginSettingsModel;
    populateDirectory(pluginSettingsModel.getCustomAssetsPath());
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private void createUIComponents() {
    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    textFieldWithBrowseButton.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(ExecutionBundle.message("select.working.directory.message"), null,
      textFieldWithBrowseButton,
      Arrays.stream(ProjectManager.getInstance().getOpenProjects()).findFirst().orElse(
        ProjectManager.getInstance().getDefaultProject()
      ),
      FileChooserDescriptorFactory.createSingleFolderDescriptor(),
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
      @Override
      protected void onFileChosen(@NotNull VirtualFile chosenFile) {
        super.onFileChosen(chosenFile);
        if(pluginSettingsModel == null) return;

        pluginSettingsModel.setCustomAssetsPath(textFieldWithBrowseButton.getText());
        populateDirectory(textFieldWithBrowseButton.getText());
      }
    });
    this.selectDir = LabeledComponent.create(textFieldWithBrowseButton,
      ExecutionBundle.message("run.configuration.working.directory.label"));
  }
}
