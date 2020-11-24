package io.unthrottled.amii.config.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll {

  private JPanel rootPanel;
  private JTabbedPane optionsPane;
  private JRadioButton radioButton1;
  private JRadioButton radioButton2;
  private JPanel anchorPanel;

  private void createUIComponents() {
    anchorPanel = new JPanel();
    // TODO: place custom component creation code here
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
    return rootPanel;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }
}
