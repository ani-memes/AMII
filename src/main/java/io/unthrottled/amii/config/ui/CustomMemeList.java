package io.unthrottled.amii.config.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import io.unthrottled.amii.assets.LocalVisualContentManager;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.assets.VisualEntityRepository;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.tools.PluginMessageBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Todo:
 * - Cultured
 * - Only Use Custom Assets
 * - Auto Tag Directories
 * - Sync Local Asset list action.
 * - Soft Deleteπ
 * - Pretty
 */
public class CustomMemeList {
  private Consumer<MemeAsset> onTest;
  private ConfigSettingsModel pluginSettingsModel;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;
  private JPanel selectDir;
  private JCheckBox createAutoLabeledDirectoriesCheckBox;
  private ActionButton refreshButton;
  private JCheckBox onlyShowUntaggedItemsCheckBox;

  public CustomMemeList(
    Consumer<MemeAsset> onTest,
    ConfigSettingsModel pluginSettingsModel
  ) {
    this.onTest = onTest;
    this.pluginSettingsModel = pluginSettingsModel;
    ayyLmao.setLayout(new BoxLayout(ayyLmao, BoxLayout.PAGE_AXIS));
    onlyShowUntaggedItemsCheckBox.addActionListener(a ->
      populateDirectory(textFieldWithBrowseButton.getText()));
  }

  private void populateDirectory(String workingDirectory) {
    if (workingDirectory.isBlank()) {
      return;
    }

    removePreExistingStuff();

    Map<String, VisualAssetRepresentation> userReps = LocalVisualContentManager.INSTANCE.supplyUserModifiedVisualRepresentations()
      .stream()
      .collect(Collectors.toMap(
        VisualAssetRepresentation::getId,
        Function.identity(),
        (a, b) -> a
      ));

    LocalVisualContentManager.supplyAllVisualAssetDefinitionsFromWorkingDirectory(workingDirectory)
      .stream()
      .map(guy -> {
        VisualAssetRepresentation userGuy = userReps.get(guy.getId());
        if(userGuy != null) {
          return userGuy.duplicateWithNewPath(guy.getPath());
        } else {
          return guy;
        }
      })
      .filter(rep ->
        !onlyShowUntaggedItemsCheckBox.isSelected() ||
          rep.getCat().isEmpty()
      )
      .forEach(visualAssetRepresentation -> {
        CustomMemePanel customMemePanel = new CustomMemePanel(
          this.onTest,
          visualAssetRepresentation
        );
        ayyLmao.add(customMemePanel.getComponent());
      });

    VisualEntityRepository.Companion.getInstance().refreshLocalAssets();
  }

  private void removePreExistingStuff() {
    while(ayyLmao.getComponentCount() > 0) {
      ayyLmao.remove(0);
    }
  }

  public void setPluginSettingsModel(ConfigSettingsModel pluginSettingsModel) {
    this.pluginSettingsModel = pluginSettingsModel;
    String customAssetsPath = pluginSettingsModel.getCustomAssetsPath();
    populateDirectory(customAssetsPath);
    textFieldWithBrowseButton.setText(customAssetsPath);
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private TextFieldWithBrowseButton textFieldWithBrowseButton;
  private void createUIComponents() {
    textFieldWithBrowseButton = new TextFieldWithBrowseButton();
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
        if (pluginSettingsModel == null) return;

        pluginSettingsModel.setCustomAssetsPath(textFieldWithBrowseButton.getText());
        populateDirectory(textFieldWithBrowseButton.getText());
      }
    });
    this.selectDir = LabeledComponent.create(textFieldWithBrowseButton,
      PluginMessageBundle.message("amii.settings.custom.assets.directory.label"));

    DumbAwareAction action = new DumbAwareAction(
      PluginMessageBundle.message("amii.settings.custom.assets.refresh.title"),
      PluginMessageBundle.message("amii.settings.custom.assets.refresh.description"),
      AllIcons.Actions.Refresh
    ) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        populateDirectory(pluginSettingsModel.getCustomAssetsPath());
      }
    };
    refreshButton = new ActionButton(action,
      action.getTemplatePresentation(),
      ActionPlaces.UNKNOWN,
      ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
  }
}
