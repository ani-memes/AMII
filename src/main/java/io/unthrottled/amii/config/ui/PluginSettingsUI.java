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
import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private JPanel rootPanel;
  private JTabbedPane optionsPane;
  private JRadioButton timedDismissRadioButton;
  private JRadioButton focusLossRadioButon;
  private JPanel anchorPanel;
  private ConfigSettingsModel initialSettings = PluginSettings.getInitialConfigSettingsModel();
  private ConfigSettingsModel pluginSettingsModel = PluginSettings.getInitialConfigSettingsModel();

  private void createUIComponents() {
    anchorPanel = AnchorPanelFactory.getAnchorPositionPanel(
      Config.getInstance().getNotificationAnchor(), notificationAnchor ->
        pluginSettingsModel.setNotificationAnchorValue(notificationAnchor.name())
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
    PanelDismissalOptions notificationMode = Config.getInstance().getNotificationMode();
    timedDismissRadioButton.setSelected(PanelDismissalOptions.TIMED.equals(notificationMode));
    focusLossRadioButon.setSelected(PanelDismissalOptions.FOCUS_LOSS.equals(notificationMode));
    ActionListener dismissalListener = a -> pluginSettingsModel.setNotificationModeValue(
      timedDismissRadioButton.isSelected() ?
        PanelDismissalOptions.TIMED.name() :
        PanelDismissalOptions.FOCUS_LOSS.name()
    );
    timedDismissRadioButton.addActionListener(dismissalListener);
    focusLossRadioButon.addActionListener(dismissalListener);
    return rootPanel;
  }

  @Override
  public boolean isModified() {
    return !initialSettings.equals(pluginSettingsModel);
  }

  @Override
  public void apply() {
    Config.getInstance().setNotificationAnchorValue(pluginSettingsModel.getNotificationAnchorValue());
    Config.getInstance().setNotificationModeValue(pluginSettingsModel.getNotificationModeValue());
    ApplicationManager.getApplication().getMessageBus().syncPublisher(
      ConfigListener.Companion.getCONFIG_TOPIC()
    ).pluginConfigUpdated(Config.getInstance());
  }
}
