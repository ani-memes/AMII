package io.unthrottled.amii.config.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import io.unthrottled.amii.assets.CharacterEntity;
import io.unthrottled.amii.assets.Gender;
import io.unthrottled.amii.config.Config;
import io.unthrottled.amii.config.ConfigListener;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.config.PluginSettings;
import io.unthrottled.amii.memes.PanelDismissalOptions;
import io.unthrottled.amii.tools.PluginMessageBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

import static io.unthrottled.amii.memes.PanelDismissalOptions.FOCUS_LOSS;
import static io.unthrottled.amii.memes.PanelDismissalOptions.TIMED;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private final ConfigSettingsModel initialSettings = PluginSettings.getInitialConfigSettingsModel();
  private final ConfigSettingsModel pluginSettingsModel = PluginSettings.getInitialConfigSettingsModel();
  private JPanel rootPanel;
  private JTabbedPane optionsPane;
  private JRadioButton timedDismissRadioButton;
  private JRadioButton focusLossRadioButton;
  private JPanel anchorPanel;
  private JSpinner timedMemeDurationSpinner;
  private JSpinner invulnerablilityDurationSpinner;
  private JCheckBox soundEnabled;
  private JSlider volumeSlider;
  private JSpinner eventsBeforeFrustrationSpinner;
  private JSlider frustrationProbabilitySlider;
  private JCheckBox allowFrustrationCheckBox;
  private JCheckBox ignoreCaseCheckBox;
  private JTextField logWatcherKeywordTextField;
  private JCheckBox enableLogWatcherCheckBox;
  private JCheckBox checkBox1;
  private JCheckBox checkBox2;
  private JCheckBox checkBox3;
  private JCheckBox checkBox4;
  private JCheckBox checkBox5;
  private JSpinner idleTimeoutSpinner;
  private JPanel exitCodePanel;
  private JCheckBox checkBox6;
  private JCheckBox preferFemale;
  private JCheckBox preferMale;
  private JCheckBox preferOther;
  private JPanel preferredCharacters;
  private PreferredCharacterPanel characterModel;


  private JBTable exitCodeTable;
  private ListTableModel<Integer> exitCodeListModel;
  private boolean exitCodesChanged = false;

  private void createUIComponents() {
    anchorPanel = AnchorPanelFactory.getAnchorPositionPanel(
      Config.getInstance().getNotificationAnchor(), notificationAnchor ->
        pluginSettingsModel.setMemeDisplayAnchorValue(notificationAnchor.name())
    );

    characterModel = new PreferredCharacterPanel();
    preferredCharacters = characterModel.getComponent();
    preferredCharacters.setPreferredSize(JBUI.size(800, 600));
    preferredCharacters.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    exitCodeListModel = new ListTableModel<Integer>() {
      @Override
      public void addRow() {
        addRow(0);
      }
    };
    exitCodeListModel.addTableModelListener(e -> exitCodesChanged = true);
    exitCodeTable = new JBTable(exitCodeListModel);
    exitCodeListModel.setColumnInfos(new ColumnInfo[]{new ColumnInfo<Integer, String>("Exit Code") {

      @Override
      public String valueOf(Integer exitCode) {
        return exitCode.toString();
      }

      @Override
      public void setValue(Integer s, String value) {
        int currentRowIndex = exitCodeTable.getSelectedRow();
        if (StringUtil.isEmpty(value) && currentRowIndex >= 0 &&
          currentRowIndex < exitCodeListModel.getRowCount()) {
          exitCodeListModel.removeRow(currentRowIndex);
        } else {
          exitCodeListModel.insertRow(currentRowIndex, Integer.parseInt(value));
          exitCodeListModel.removeRow(currentRowIndex + 1);
          exitCodeTable.transferFocus();
        }
      }

      @Override
      public boolean isCellEditable(Integer info) {
        return true;
      }
    }});
    exitCodeTable.getColumnModel().setColumnMargin(0);
    exitCodeTable.setShowColumns(false);
    exitCodeTable.setShowGrid(false);

    exitCodeTable.getEmptyText().setText(PluginMessageBundle.message("settings.exit.code.no.codes"));

    exitCodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    exitCodePanel = ToolbarDecorator.createDecorator(exitCodeTable)
      .disableUpDownActions().createPanel();
  }

  @Override
  public @NotNull String getId() {
    return "io.unthrottled.amii.config.PluginSettings";
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return "AMII Settings";
  }

  @Override
  public @Nullable JComponent createComponent() {
    Config config = Config.getInstance();

    PanelDismissalOptions notificationMode = config.getNotificationMode();
    timedDismissRadioButton.setSelected(TIMED.equals(notificationMode));
    focusLossRadioButton.setSelected(FOCUS_LOSS.equals(notificationMode));
    enableCorrectSpinner(notificationMode);
    ActionListener dismissalListener = a -> {
      PanelDismissalOptions memeDisplayModeValue = timedDismissRadioButton.isSelected() ?
        TIMED :
        FOCUS_LOSS;
      enableCorrectSpinner(memeDisplayModeValue);
      pluginSettingsModel.setMemeDisplayModeValue(
        memeDisplayModeValue.name()
      );
    };
    timedDismissRadioButton.addActionListener(dismissalListener);
    focusLossRadioButton.addActionListener(dismissalListener);

    SpinnerNumberModel timedMemeDurationModel = new SpinnerNumberModel(
      config.getMemeDisplayTimedDuration(),
      10,
      Integer.MAX_VALUE,
      1
    );
    timedMemeDurationSpinner.setModel(timedMemeDurationModel);
    timedMemeDurationSpinner.addChangeListener(change ->
      pluginSettingsModel.setMemeDisplayTimedDuration(
        timedMemeDurationModel.getNumber().intValue()
      ));

    SpinnerNumberModel invulnerabilityDurationModel = new SpinnerNumberModel(
      config.getMemeDisplayInvulnerabilityDuration(),
      0,
      Integer.MAX_VALUE,
      1
    );
    invulnerablilityDurationSpinner.setModel(invulnerabilityDurationModel);
    invulnerablilityDurationSpinner.addChangeListener(change ->
      pluginSettingsModel.setMemeDisplayInvulnerabilityDuration(
        invulnerabilityDurationModel.getNumber().intValue()
      ));

    allowFrustrationCheckBox.addActionListener(e -> {
      frustrationProbabilitySlider.setEnabled(allowFrustrationCheckBox.isSelected());
      eventsBeforeFrustrationSpinner.setEnabled(allowFrustrationCheckBox.isSelected());
    });
    frustrationProbabilitySlider.setForeground(UIUtil.getContextHelpForeground());

    soundEnabled.addActionListener(e -> volumeSlider.setEnabled(soundEnabled.isSelected()));
    volumeSlider.setForeground(UIUtil.getContextHelpForeground());
    volumeSlider.addChangeListener(
      e -> pluginSettingsModel.setMemeVolume(((JSlider) e.getSource()).getModel().getValue())
    );

    preferFemale.addActionListener(e -> updateGenderPreference(Gender.FEMALE.getValue(), preferFemale.isSelected()));
    preferMale.addActionListener(e -> updateGenderPreference(Gender.MALE.getValue(), preferMale.isSelected()));
    preferOther.addActionListener(e -> updateGenderPreference(Gender.OTHER.getValue(), preferOther.isSelected()));

    initFromState();
    return rootPanel;
  }

  private void updateGenderPreference(int value, boolean selected) {
    int preferredGenders = pluginSettingsModel.getPreferredGenders();
    pluginSettingsModel.setPreferredGenders(
      selected ?
        preferredGenders | value :
        preferredGenders ^ value
    );
  }

  private void initFromState() {
    soundEnabled.setSelected(initialSettings.getSoundEnabled());
    volumeSlider.getModel().setValue(initialSettings.getMemeVolume());
    preferFemale.setSelected(isGenderSelected(Gender.FEMALE.getValue()));
    preferMale.setSelected(isGenderSelected(Gender.MALE.getValue()));
    preferOther.setSelected(isGenderSelected(Gender.OTHER.getValue()));
  }

  private boolean isGenderSelected(int genderCode) {
    return (initialSettings.getPreferredGenders() & genderCode) == genderCode;
  }

  private void enableCorrectSpinner(PanelDismissalOptions memeDisplayModeValue) {
    invulnerablilityDurationSpinner.setEnabled(memeDisplayModeValue == FOCUS_LOSS);
    timedMemeDurationSpinner.setEnabled(memeDisplayModeValue == TIMED);
  }

  @Override
  public boolean isModified() {
    return !initialSettings.equals(pluginSettingsModel) ||
      characterModel.isModified();
  }

  @Override
  public void apply() {
    Config config = Config.getInstance();
    config.setMemeDisplayAnchorValue(pluginSettingsModel.getMemeDisplayAnchorValue());
    config.setMemeDisplayModeValue(pluginSettingsModel.getMemeDisplayModeValue());
    config.setMemeDisplayInvulnerabilityDuration(pluginSettingsModel.getMemeDisplayInvulnerabilityDuration());
    config.setMemeDisplayTimedDuration(pluginSettingsModel.getMemeDisplayTimedDuration());
    config.setPreferredCharacters(
      characterModel.getSelected().stream()
        .map(CharacterEntity::getId)
        .collect(Collectors.joining(Config.DEFAULT_DELIMITER))
    );
    config.setSoundEnabled(pluginSettingsModel.getSoundEnabled());
    config.setMemeVolume(pluginSettingsModel.getMemeVolume());
    config.setPreferredGenders(pluginSettingsModel.getPreferredGenders());
    ApplicationManager.getApplication().getMessageBus().syncPublisher(
      ConfigListener.Companion.getCONFIG_TOPIC()
    ).pluginConfigUpdated(config);
  }
}
