package io.unthrottled.amii.config.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import io.unthrottled.amii.config.Config;
import io.unthrottled.amii.config.ConfigListener;
import io.unthrottled.amii.config.ConfigSettingsModel;
import io.unthrottled.amii.config.PluginSettings;
import io.unthrottled.amii.memes.PanelDismissalOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private final ConfigSettingsModel initialSettings = PluginSettings.getInitialConfigSettingsModel();
  private final ConfigSettingsModel pluginSettingsModel = PluginSettings.getInitialConfigSettingsModel();
  private JPanel rootPanel;
  private JTabbedPane optionsPane;
  private JRadioButton timedDismissRadioButton;
  private JRadioButton focusLossRadioButton;
  private JPanel anchorPanel;
  private JSpinner timedMemeDuration;
  private JSpinner invulnerablilityDurationSpinner;

  private void createUIComponents() {
    anchorPanel = AnchorPanelFactory.getAnchorPositionPanel(
      Config.getInstance().getNotificationAnchor(), notificationAnchor ->
        pluginSettingsModel.setMemeDisplayAnchorValue(notificationAnchor.name())
    );
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
    timedDismissRadioButton.setSelected(PanelDismissalOptions.TIMED.equals(notificationMode));
    focusLossRadioButton.setSelected(PanelDismissalOptions.FOCUS_LOSS.equals(notificationMode));
    ActionListener dismissalListener = a -> pluginSettingsModel.setMemeDisplayModeValue(
      timedDismissRadioButton.isSelected() ?
        PanelDismissalOptions.TIMED.name() :
        PanelDismissalOptions.FOCUS_LOSS.name()
    );
    timedDismissRadioButton.addActionListener(dismissalListener);
    focusLossRadioButton.addActionListener(dismissalListener);

    SpinnerNumberModel timedMemeDurationModel = new SpinnerNumberModel(
      config.getMemeDisplayTimedDuration(),
      10,
      Integer.MAX_VALUE,
      1
    );
    timedMemeDuration.setModel(timedMemeDurationModel);
    timedMemeDuration.addChangeListener(change ->
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

    return rootPanel;
  }

  @Override
  public boolean isModified() {
    return !initialSettings.equals(pluginSettingsModel);
  }

  @Override
  public void apply() {
    Config config = Config.getInstance();
    config.setMemeDisplayAnchorValue(pluginSettingsModel.getMemeDisplayAnchorValue());
    config.setMemeDisplayModeValue(pluginSettingsModel.getMemeDisplayModeValue());
    config.setMemeDisplayInvulnerabilityDuration(pluginSettingsModel.getMemeDisplayInvulnerabilityDuration());
    config.setMemeDisplayTimedDuration(pluginSettingsModel.getMemeDisplayTimedDuration());
    ApplicationManager.getApplication().getMessageBus().syncPublisher(
      ConfigListener.Companion.getCONFIG_TOPIC()
    ).pluginConfigUpdated(config);
  }
}
