package io.unthrottled.amii.config.ui;

import com.intellij.openapi.application.ApplicationManager;
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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
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
  private MemeCategoriesComponent memeCategoriesComponent;
  private JPanel audioAssetPath;
  private JCheckBox isCulturedCheckBox;
  private TextFieldWithBrowseButton audioAssetTextField;

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
        .ifPresent(asset -> {
            String fullAudioPath = Paths.get(asset.getFilePath()).toAbsolutePath().toString();
            this.audioAssetTextField.setText(fullAudioPath);
            this.audioAssetURL = fullAudioPath;
          }
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

    memeCategoriesComponent.reset(
      new MemeCategoryState(visualEntity.getAssetCategories())
    );

    final URI assetUri = URI.create(visualAssetRepresentation.getPath());
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
        String extraStyles = AssetTools.getDimensionCappingStyle(
          assetUri,
          new Dimension(225, 225)
        );
        @Language("HTML") String meme = "<html><img src=\"" + assetUri + "\" " + extraStyles + "/></html>";
        SwingUtilities.invokeLater(() -> memeDisplay.setText(meme));
    });

    isCulturedCheckBox.setSelected(visualEntity.isLewd());
    isCulturedCheckBox.addActionListener(a -> {
      VisualAssetRepresentation repToChange = visualEntity.getRepresentation()
        .culturedDuplicate(isCulturedCheckBox.isSelected());

      visualEntity = visualEntity.duplicate(
        visualEntity.getAssetCategories(),
        visualEntity.getAudibleAssetId(),
        repToChange
      );
      VisualEntityRepository.Companion.getInstance().update(visualEntity);
    });

    testMeme.addActionListener(a ->
      onTest.accept(
        new MemeAsset(
          new VisualMemeContent(
            visualAssetRepresentation.getId(),
            assetUri,
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
    Pair<JPanel, MemeCategoriesComponent> component = MemeCategoriesPanel.createComponent();
    categoriesPanel = component.getFirst();
    memeCategoriesComponent = component.getSecond();
    memeCategoriesComponent.onUpdate(
      categories -> {
        VisualAssetRepresentation representation = visualEntity.getRepresentation().duplicate(
          categories.stream().map(MemeAssetCategory::getValue).collect(Collectors.toList()),
          visualEntity.getRepresentation().getAud()
        );
        visualEntity = visualEntity.duplicate(
          categories,
          visualEntity.getAudibleAssetId(),
          representation
        );
        VisualEntityRepository.Companion.getInstance().update(visualEntity);
      }
    );

    audioAssetTextField = new TextFieldWithBrowseButton();
    audioAssetTextField.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(PluginMessageBundle.message("select.working.directory.message"), null,
      audioAssetTextField,
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
    this.audioAssetPath = LabeledComponent.create(audioAssetTextField,
      PluginMessageBundle.message("settings.custom.assets.audio.asset.label"));
  }
}
