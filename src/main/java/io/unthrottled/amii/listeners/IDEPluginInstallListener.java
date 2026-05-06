package io.unthrottled.amii.listeners;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import io.unthrottled.amii.PluginMaster;

import static io.unthrottled.amii.config.Constants.PLUGIN_ID;

public class IDEPluginInstallListener implements DynamicPluginListener {
  @Override
  public void pluginLoaded(IdeaPluginDescriptor pluginDescriptor) {
    if (PLUGIN_ID.equals(pluginDescriptor.getPluginId().getIdString())) {
      ApplicationManager.getApplication().invokeLater(() -> PluginMaster.Companion.getInstance().onUpdate());
    }
  }
}
