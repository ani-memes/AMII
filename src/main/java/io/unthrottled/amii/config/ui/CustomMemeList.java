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
import com.intellij.ui.components.JBList;
import io.unthrottled.amii.assets.AssetFetchOptions;
import io.unthrottled.amii.assets.LocalVisualContentManager;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.assets.VisualEntityRepository;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.tools.PluginMessageBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CustomMemeList {
  private static final Logger logger = Logger.getInstance(CustomMemeList.class);
  private Consumer<MemeAsset> onTest;
  private ConfigSettingsModel pluginSettingsModel;
  private JPanel rootPane;
  private JPanel ayyLmao;
  private JPanel selectDir;
  private JCheckBox createAutoLabeledDirectoriesCheckBox;
  private ActionButton refreshButton;
  private JCheckBox onlyShowUntaggedItemsCheckBox;
  private JCheckBox allowSuggestiveContentCheckBox;
  private JCheckBox onlyUseCustomAssetsCheckBox;
  private final DefaultListModel<VisualAssetRepresentation> assetListModel = new DefaultListModel<>();
  private final AtomicInteger scanGeneration = new AtomicInteger();
  private JBList<VisualAssetRepresentation> assetList;
  private JPanel assetEditorPanel;

  public CustomMemeList(
    Consumer<MemeAsset> onTest,
    ConfigSettingsModel pluginSettingsModel
  ) {
    this.onTest = onTest;
    this.pluginSettingsModel = pluginSettingsModel;
    initializeAssetBrowser();
    onlyShowUntaggedItemsCheckBox.addActionListener(a ->
      populateDirectory(textFieldWithBrowseButton.getText()));
    allowSuggestiveContentCheckBox.addActionListener(a -> {
      this.pluginSettingsModel.setAllowLewds(allowSuggestiveContentCheckBox.isSelected());
      populateDirectory(this.pluginSettingsModel.getCustomAssetsPath());
    });
    onlyUseCustomAssetsCheckBox.addActionListener(a ->
      this.pluginSettingsModel.setOnlyCustomAssets(onlyUseCustomAssetsCheckBox.isSelected()));
    createAutoLabeledDirectoriesCheckBox.addActionListener(a -> {
      this.pluginSettingsModel.setCreateAutoTagDirectories(createAutoLabeledDirectoriesCheckBox.isSelected());
      createAutoTagDirectories(this.pluginSettingsModel);
    });
  }

  private void populateDirectory(String workingDirectory) {
    if (workingDirectory.isBlank()) {
      clearAssetBrowser();
      return;
    }

    showLoadingState();
    int currentGeneration = scanGeneration.incrementAndGet();
    boolean includeLewds = this.pluginSettingsModel.getAllowLewds();
    boolean onlyShowUntaggedItems = onlyShowUntaggedItemsCheckBox.isSelected();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      Set<VisualAssetRepresentation> visualAssetRepresentations = LocalVisualContentManager.supplyAllVisualAssetDefinitionsFromWorkingDirectory(
        new AssetFetchOptions(
          workingDirectory,
          includeLewds
        )
      );
      VisualEntityRepository.Companion.getInstance().refreshLocalAssets();

      SwingUtilities.invokeLater(()->{
        if (currentGeneration != scanGeneration.get()) {
          return;
        }

        List<VisualAssetRepresentation> assets = visualAssetRepresentations.stream()
          .filter(rep ->
            !onlyShowUntaggedItems ||
              rep.getCat().isEmpty()
          )
          .sorted(Comparator.comparing(VisualAssetRepresentation::getPath))
          .collect(Collectors.toList());
        updateAssetList(assets);
      });
    });
  }

  private void initializeAssetBrowser() {
    ayyLmao.setLayout(new BorderLayout());

    assetList = new JBList<>(assetListModel);
    assetList.setCellRenderer(new AssetListCellRenderer());
    assetList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    assetList.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        showSelectedAsset(assetList.getSelectedValue());
      }
    });

    assetEditorPanel = new JPanel(new BorderLayout());
    assetEditorPanel.add(createPlaceholder("Select a custom asset to edit."), BorderLayout.CENTER);

    JSplitPane assetBrowser = new JSplitPane(
      JSplitPane.HORIZONTAL_SPLIT,
      new JScrollPane(assetList),
      assetEditorPanel
    );
    assetBrowser.setResizeWeight(0.25);
    ayyLmao.add(assetBrowser, BorderLayout.CENTER);
  }

  private void clearAssetBrowser() {
    scanGeneration.incrementAndGet();
    assetListModel.clear();
    showSelectedAsset(null);
  }

  private void showLoadingState() {
    assetListModel.clear();
    showPlaceholder("Scanning custom assets...");
  }

  private void updateAssetList(List<VisualAssetRepresentation> assets) {
    assetListModel.clear();
    assets.forEach(assetListModel::addElement);
    if (assets.isEmpty()) {
      showPlaceholder("No custom GIF assets found.");
    } else {
      assetList.setSelectedIndex(0);
    }
  }

  private void showSelectedAsset(VisualAssetRepresentation asset) {
    assetEditorPanel.removeAll();
    if (asset == null) {
      assetEditorPanel.add(createPlaceholder("Select a custom asset to edit."), BorderLayout.CENTER);
    } else {
      CustomMemePanel customMemePanel = new CustomMemePanel(
        this.onTest,
        asset
      );
      assetEditorPanel.add(customMemePanel.getComponent(), BorderLayout.CENTER);
    }
    assetEditorPanel.revalidate();
    assetEditorPanel.repaint();
  }

  private void showPlaceholder(String message) {
    assetEditorPanel.removeAll();
    assetEditorPanel.add(createPlaceholder(message), BorderLayout.CENTER);
    assetEditorPanel.revalidate();
    assetEditorPanel.repaint();
  }

  private JComponent createPlaceholder(String message) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel(message), BorderLayout.CENTER);
    return panel;
  }

  public void setPluginSettingsModel(ConfigSettingsModel pluginSettingsModel) {
    this.pluginSettingsModel = pluginSettingsModel;
    String customAssetsPath = pluginSettingsModel.getCustomAssetsPath();
    textFieldWithBrowseButton.setText(customAssetsPath);
    allowSuggestiveContentCheckBox.setSelected(pluginSettingsModel.getAllowLewds());
    onlyUseCustomAssetsCheckBox.setSelected(pluginSettingsModel.getOnlyCustomAssets());
    createAutoLabeledDirectoriesCheckBox.setSelected(pluginSettingsModel.getCreateAutoTagDirectories());
    createAutoTagDirectories(pluginSettingsModel);
  }

  private void createAutoTagDirectories(ConfigSettingsModel pluginSettingsModel) {
    String customAssetsPath = pluginSettingsModel.getCustomAssetsPath();
    if (pluginSettingsModel.getCreateAutoTagDirectories() && !customAssetsPath.isBlank()) {
      LocalVisualContentManager.INSTANCE.createAutoTagDirectories(customAssetsPath);
    }
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

  private boolean loaded = false;
  public void load() {
    if(!loaded) {
      populateDirectory(this.pluginSettingsModel.getCustomAssetsPath());
      loaded = true;
    }
  }

  private static class AssetListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
      JList<?> list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
    ) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof VisualAssetRepresentation asset) {
        label.setText(getDisplayName(asset));
        label.setToolTipText(asset.getPath());
      }
      return label;
    }

    private String getDisplayName(VisualAssetRepresentation asset) {
      try {
        return Paths.get(URI.create(asset.getPath())).getFileName().toString();
      } catch (RuntimeException ignored) {
        try {
          return Paths.get(asset.getPath()).getFileName().toString();
        } catch (RuntimeException ignoredAgain) {
          return asset.getPath();
        }
      }
    }
  }
}
