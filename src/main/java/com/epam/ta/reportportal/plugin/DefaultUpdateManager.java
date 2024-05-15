package com.epam.ta.reportportal.plugin;

import org.pf4j.PluginManager;
import org.pf4j.update.UpdateManager;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class DefaultUpdateManager extends UpdateManager {

  public DefaultUpdateManager(PluginManager pluginManager) {
    super(pluginManager);
  }

  @Override
  public Path downloadPlugin(String id, String version) {
    return super.downloadPlugin(id, version);
  }
}

