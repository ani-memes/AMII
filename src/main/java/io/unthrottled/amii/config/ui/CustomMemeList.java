package io.unthrottled.amii.config.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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
import io.unthrottled.amii.assets.MemeAssetCategory;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.assets.VisualEntityRepository;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.tools.AssetTools;
import io.unthrottled.amii.tools.PluginMessageBundle;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Todo:
 * - Soft Delete
 * - Pretty
 */
public class CustomMemeList {
  private static final Logger logger = Logger.getInstance(CustomMemeList.class);
  private Consumer<MemeAsset> onTest;
  private ConfigSettingsModel pluginSettingsModel;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;
  private JPanel selectDir;
  private JCheckBox createAutoLabeledDirectoriesCheckBox;
  private ActionButton refreshButton;
  private JCheckBox onlyShowUntaggedItemsCheckBox;
  private JCheckBox allowSuggestiveContentCheckBox;
  private JCheckBox onlyUseCustomAssetsCheckBox;

  public CustomMemeList(
    Consumer<MemeAsset> onTest,
    ConfigSettingsModel pluginSettingsModel
  ) {
    this.onTest = onTest;
    this.pluginSettingsModel = pluginSettingsModel;
    ayyLmao.setLayout(new BoxLayout(ayyLmao, BoxLayout.PAGE_AXIS));
    onlyShowUntaggedItemsCheckBox.addActionListener(a ->
      populateDirectory(textFieldWithBrowseButton.getText()));
    allowSuggestiveContentCheckBox.addActionListener(a ->
      this.pluginSettingsModel.setAllowLewds(allowSuggestiveContentCheckBox.isSelected()));
    onlyUseCustomAssetsCheckBox.addActionListener(a ->
      this.pluginSettingsModel.setOnlyCustomAssets(onlyUseCustomAssetsCheckBox.isSelected()));
    createAutoLabeledDirectoriesCheckBox.addActionListener(a -> {
      this.pluginSettingsModel.setCreateAutoTagDirectories(createAutoLabeledDirectoriesCheckBox.isSelected());
      createAutoTagDirectories(this.pluginSettingsModel);
    });
  }

  private void populateDirectory(String workingDirectory) {
    if (workingDirectory.isBlank()) {
      return;
    }

    removePreExistingStuff();

    // todo: ensure that all local asset work is done
    // not on the AWT thread.
    ApplicationManager.getApplication().executeOnPooledThread(() ->
      getVisualAssetRepresentationStream(workingDirectory)
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

          VisualEntityRepository.Companion.getInstance().refreshLocalAssets();
        }));
  }

  @NotNull
  private Stream<VisualAssetRepresentation> getVisualAssetRepresentationStream(String workingDirectory) {
    if (this.pluginSettingsModel.getCreateAutoTagDirectories()) {
      autoTagAssets(workingDirectory);
    }
    return LocalVisualContentManager.supplyAllVisualAssetDefinitionsFromWorkingDirectory(
        workingDirectory
      )
      .stream();
  }

  private static void autoTagAssets(String workingDirectory) {
    Map<String, VisualAssetRepresentation> allLocalAssets = LocalVisualContentManager.supplyAllVisualAssetDefinitionsFromWorkingDirectory(workingDirectory)
      .stream()
      .collect(Collectors.toMap(
        VisualAssetRepresentation::getId,
        Function.identity(),
        (a, b) -> a
      ));
    Map<Boolean, List<VisualAssetRepresentation>> partitionedAutoTagAssets = getAutoTagDirectories(workingDirectory)
      .flatMap(stuff -> {
        try {
          return LocalVisualContentManager.walkDirectoryForAssets(
              stuff.getFirst().toString()
            )
            .map(assetPath -> allLocalAssets.get(AssetTools.calculateMD5Hash(assetPath)))
            .filter(Objects::nonNull)
            .map(rep -> {
              MemeAssetCategory memeAssetCategory = stuff.getSecond();
              int memeAssetCategoryValue = memeAssetCategory.getValue();
              if (!rep.getCat().contains(memeAssetCategoryValue)) {
                rep.getCat().add(memeAssetCategoryValue);
                return new Pair<>(rep, true);
              } else {
                return new Pair<>(rep, false);
              }
            });
        } catch (RuntimeException e) {
          logger.warn("Unable to auto tag assets for dir " + stuff.getFirst(), e);
          return Stream.empty();
        }
      }).collect(Collectors.partitioningBy(
        Pair::getSecond, Collectors.mapping(Pair::getFirst, Collectors.toList())
      ));

    List<VisualAssetRepresentation> assetsToUpdate = partitionedAutoTagAssets.getOrDefault(true, Collections.emptyList());
    if (!assetsToUpdate.isEmpty()) {
      LocalVisualContentManager.INSTANCE.updateRepresentations(
        assetsToUpdate
      );
      VisualEntityRepository.Companion.getInstance().refreshLocalAssets();
    }
  }

  private void removePreExistingStuff() {
    while (ayyLmao.getComponentCount() > 0) {
      ayyLmao.remove(0);
    }
  }

  public void setPluginSettingsModel(ConfigSettingsModel pluginSettingsModel) {
    this.pluginSettingsModel = pluginSettingsModel;
    String customAssetsPath = pluginSettingsModel.getCustomAssetsPath();
    populateDirectory(customAssetsPath);
    textFieldWithBrowseButton.setText(customAssetsPath);
    allowSuggestiveContentCheckBox.setSelected(pluginSettingsModel.getAllowLewds());
    onlyUseCustomAssetsCheckBox.setSelected(pluginSettingsModel.getOnlyCustomAssets());
    createAutoLabeledDirectoriesCheckBox.setSelected(pluginSettingsModel.getCreateAutoTagDirectories());
    createAutoTagDirectories(pluginSettingsModel);
  }

  private void createAutoTagDirectories(ConfigSettingsModel pluginSettingsModel) {
    String customAssetsPath = pluginSettingsModel.getCustomAssetsPath();
    if (!pluginSettingsModel.getCreateAutoTagDirectories() || customAssetsPath.isBlank()) {
      return;
    }

    getAutoTagDirectories(customAssetsPath)
      .map(Pair::component1)
      .filter(
        autoTagDirectory -> !Files.exists(autoTagDirectory)
      ).forEach(
        autoTagDirectory -> {
          try {
            Files.createDirectories(autoTagDirectory);
          } catch (IOException e) {
            logger.warn("Unable to create auto tag dir " + autoTagDirectory, e);
          }
        }
      );
  }

  @NotNull
  private static Stream<Pair<Path, MemeAssetCategory>> getAutoTagDirectories(String customAssetsPath) {
    return Arrays.stream(MemeAssetCategory.values())
      .map(cat ->
        new Pair<>(
          Paths.get(
            customAssetsPath, cat.name().toLowerCase(Locale.ROOT)
          ),
          cat
        ));
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
        createAutoTagDirectories(pluginSettingsModel);
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
