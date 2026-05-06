package io.unthrottled.amii.tools;

import com.intellij.openapi.extensions.PluginId;

public final class PluginIds {
  private PluginIds() {
  }

  public static PluginId getId(String id) {
    return PluginId.getId(id);
  }
}
