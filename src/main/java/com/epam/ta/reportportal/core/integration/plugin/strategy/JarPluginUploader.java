package com.epam.ta.reportportal.core.integration.plugin.strategy;

import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JarPluginUploader implements PluginUploader {

  private final Pf4jPluginBox pluginBox;

  @Autowired
  public JarPluginUploader(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  @Override
  public IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException {
    return pluginBox.uploadPlugin(fileName, inputStream);
  }
}