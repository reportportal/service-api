/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.plugin;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;

public class Pf4jPluginManager implements Pf4jPluginBox {

  public static final Logger LOGGER = LoggerFactory.getLogger(Pf4jPluginManager.class);

  public static final String LOAD_KEY = "load";
  public static final String UNLOAD_KEY = "unload";

  private final PluginManager pluginManager;
  private final AutowireCapableBeanFactory autowireCapableBeanFactory;

  private final ApplicationEventPublisher applicationEventPublisher;

  public Pf4jPluginManager(PluginManager pluginManager,
      AutowireCapableBeanFactory autowireCapableBeanFactory,
      ApplicationEventPublisher applicationEventPublisher) {
    this.pluginManager = pluginManager;
    this.autowireCapableBeanFactory = autowireCapableBeanFactory;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public List<Plugin> getPlugins() {
    return this.pluginManager.getPlugins()
        .stream()
        .flatMap(plugin -> pluginManager.getExtensionClasses(plugin.getPluginId())
            .stream()
            .map(ExtensionPoint::findByExtension)
            .filter(Optional::isPresent)
            .map(it -> new Plugin(plugin.getPluginId(), it.get())))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Plugin> getPlugin(String type) {
    return getPlugins().stream().filter(p -> p.getType().name().equalsIgnoreCase(type)).findAny();
  }

  @Override
  public <T> Optional<T> getInstance(String name, Class<T> extension) {
    return pluginManager.getExtensions(extension, name).stream().findFirst();
  }

  @Override
  public <T> Optional<T> getInstance(Class<T> extension) {
    return pluginManager.getExtensions(extension).stream().findFirst();
  }

  @Override
  public PluginState startUpPlugin(Path pluginPath) {
    return loadPlugin(pluginPath).flatMap(this::getPluginById)
        .map(this::startUpPlugin)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
            Suppliers.formattedSupplier("Error during loading the plugin file = '{}'",
                    pluginPath.getFileName().toString())
                .get()
        ));
  }

  @Override
  public PluginState startUpPlugin(PluginWrapper pluginWrapper) {
    final PluginState pluginState = pluginManager.startPlugin(pluginWrapper.getPluginId());
    applicationEventPublisher.publishEvent(new PluginEvent(pluginWrapper.getPluginId(), LOAD_KEY));
    return pluginState;
  }

  @Override
  public Optional<String> loadPlugin(Path pluginPath) {
    return ofNullable(pluginManager.loadPlugin(pluginPath));
  }

  @Override
  public boolean unloadPlugin(PluginWrapper pluginWrapper) {
    applicationEventPublisher.publishEvent(
        new PluginEvent(pluginWrapper.getPluginId(), UNLOAD_KEY));
    destroyDependency(pluginWrapper.getPluginId());
    return pluginManager.unloadPlugin(pluginWrapper.getPluginId());
  }

  @Override
  public Optional<PluginWrapper> getPluginById(String id) {
    return ofNullable(pluginManager.getPlugin(id));
  }

  @Override
  public boolean deletePlugin(PluginWrapper pluginWrapper) {
    applicationEventPublisher.publishEvent(
        new PluginEvent(pluginWrapper.getPluginId(), UNLOAD_KEY));
    destroyDependency(pluginWrapper.getPluginId());
    return pluginManager.deletePlugin(pluginWrapper.getPluginId());
  }

  private void destroyDependency(String name) {
    AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) this.autowireCapableBeanFactory;
    if (beanFactory.containsSingleton(name)) {
      beanFactory.destroySingleton(name);
    }
  }

}
