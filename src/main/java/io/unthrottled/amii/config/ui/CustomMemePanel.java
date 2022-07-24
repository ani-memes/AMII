package io.unthrottled.amii.config.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import io.unthrottled.amii.assets.AudibleAssetDefinitionService;
import io.unthrottled.amii.assets.AudibleContent;
import io.unthrottled.amii.assets.AudibleRepresentation;
import io.unthrottled.amii.assets.LocalAudibleDefinitionService;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.MemeAssetCategory;
import io.unthrottled.amii.assets.VisualAssetEntity;
import io.unthrottled.amii.assets.VisualAssetRepresentation;
import io.unthrottled.amii.assets.VisualEntityRepository;
import io.unthrottled.amii.assets.VisualMemeContent;
import io.unthrottled.amii.tools.AssetTools;
import io.unthrottled.amii.tools.PluginMessageBundle;
import kotlin.Pair;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CustomMemePanel {
  private JPanel memeSettings;
  private JButton testMeme;
  private JPanel visualAssetDisplay;
  private JPanel rootPane;
  private JBLabel memeDisplay;
  private JPanel categoriesPanel;
  private JPanel audioAssetPath;

  private String audioAssetURL = null;

  private VisualAssetEntity visualEntity;

  public CustomMemePanel(
    Consumer<MemeAsset> onTest,
    VisualAssetRepresentation visualAssetRepresentation
  ) {
    VisualAssetEntity existingEntity = VisualEntityRepository.Companion.getInstance()
      .findById(visualAssetRepresentation.getId());
    if (existingEntity != null) {
      visualEntity = existingEntity;
      Optional.ofNullable(visualEntity.getAudibleAssetId())
        .flatMap(AudibleAssetDefinitionService.INSTANCE::getAssetById)
        .ifPresent(asset ->
          this.audioAssetURL = Paths.get(asset.getFilePath()).toAbsolutePath().toString()
        );
    } else {
      VisualAssetEntity entity = new VisualAssetEntity(
        visualAssetRepresentation.getId(),
        visualAssetRepresentation.getPath(),
        visualAssetRepresentation.getAlt(),
        new HashSet<>(),
        new ArrayList<>(),
        visualAssetRepresentation,
        null,
        true
      );
      VisualEntityRepository.Companion.getInstance().update(entity);
      visualEntity = entity;
    }

    String assetUri = visualAssetRepresentation.getPath();
    @Language("HTML") String meme = "<html><img src=\"" + assetUri + "\" /></html>";
    memeDisplay.setText(meme);

    testMeme.addActionListener(a ->
      onTest.accept(
        new MemeAsset(
          new VisualMemeContent(
            visualAssetRepresentation.getId(),
            URI.create(assetUri),
            "",
            null
          ),
          this.audioAssetURL == null ? null :
            new AudibleContent(
              Paths.get(this.audioAssetURL).toUri()
            )
        )
      ));
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private void createUIComponents() {
    Pair<JPanel, GrazieLanguagesComponent> component = MemeCategoriesPanel.createComponent();
    categoriesPanel = component.component1();
    component.component2().onUpdate(
      categories -> {
        VisualAssetRepresentation representation = visualEntity.getRepresentation().duplicate(
          categories.stream().map(MemeAssetCategory::getValue).collect(Collectors.toList()),
          visualEntity.getRepresentation().getId()
        );
        visualEntity = visualEntity.duplicate(
          categories,
          visualEntity.getAudibleAssetId(),
          representation
        );
        VisualEntityRepository.Companion.getInstance().update(visualEntity);
      }
    );

    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    textFieldWithBrowseButton.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(ExecutionBundle.message("select.working.directory.message"), null,
      textFieldWithBrowseButton,
      Arrays.stream(ProjectManager.getInstance().getOpenProjects()).findFirst().orElse(
        ProjectManager.getInstance().getDefaultProject()
      ),
      FileChooserDescriptorFactory.createSingleFileDescriptor(),
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
      @Override
      protected void onFileChosen(@NotNull VirtualFile chosenFile) {
        super.onFileChosen(chosenFile);
        audioAssetURL = chosenFile.getPath();
        AudibleRepresentation localAudibleContent = new AudibleRepresentation(
          AssetTools.calculateMD5Hash(chosenFile.toNioPath()),
          chosenFile.getPath(),
          false
        );
        LocalAudibleDefinitionService.INSTANCE.save(
          localAudibleContent
        );

        VisualAssetRepresentation visualAssetRepresentation = visualEntity.getRepresentation()
          .duplicate(visualEntity.getRepresentation().getCat(), localAudibleContent.getId());
        visualEntity = visualEntity.duplicate(
          visualEntity.getAssetCategories(),
          localAudibleContent.getId(),
          visualAssetRepresentation
        );
        VisualEntityRepository.Companion.getInstance().update(visualEntity);
      }
    });
    this.audioAssetPath = LabeledComponent.create(textFieldWithBrowseButton,
      PluginMessageBundle.message("settings.custom.assets.audio.asset.label"));
  }
}