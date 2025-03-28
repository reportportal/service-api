package com.epam.ta.reportportal.plugin;

import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

public class RemotePluginManager implements Pf4jPluginBox {

  @Override
  public void startUp() {}

  @Override
  public void shutDown() {}

  @Override
  public PluginState startUpPlugin(String pluginId) {
    return null;
  }

  @Override
  public boolean loadPlugin(String pluginId, IntegrationTypeDetails integrationTypeDetails) {
    return false;
  }

  @Override
  public boolean unloadPlugin(IntegrationType integrationType) {
    return false;
  }

  @Override
  public boolean deletePlugin(String pluginId) {
    return false;
  }

  @Override
  public boolean deletePlugin(PluginWrapper pluginWrapper) {
    return false;
  }

  @Override
  public Optional<PluginWrapper> getPluginById(String id) {
    return Optional.empty();
  }

  @Override
  public boolean isInUploadingState(String fileName) {
    return false;  }

  @Override
  public IntegrationType uploadPlugin(String newPluginFileName, InputStream fileStream) {
    return null;
  }

  @Override
  public List<Plugin> getPlugins() {
    return List.of();
  }

  @Override
  public Optional<Plugin> getPlugin(String type) {
    return Optional.empty();
  }

  @Override
  public <T> Optional<T> getInstance(String name, Class<T> type) {
    return Optional.empty();
  }

  @Override
  public <T> Optional<T> getInstance(Class<T> type) {
    return Optional.empty();
  }
}
