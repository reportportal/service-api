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

package com.epam.ta.reportportal.core.configs;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.plugin.DetailManifestPluginDescriptorFinder;
import com.epam.ta.reportportal.plugin.Pf4jPluginManager;
import com.epam.ta.reportportal.plugin.ReportPortalExtensionFactory;
import jakarta.activation.FileTypeMap;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.LegacyExtensionFinder;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

@Configuration
public class PluginConfiguration {

  @Autowired
  private AutowireCapableBeanFactory context;

  @Autowired
  private PluginLoader pluginLoader;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Value("${rp.plugins.path}")
  private String pluginsPath;

  @Value("${rp.plugins.temp.path}")
  private String pluginsTempPath;

  @Value("${rp.plugins.resources.path}")
  private String pluginsResourcesPath;

  @Value("${rp.plugins.resources.public}")
  private String publicFolderQualifier;

  @Bean
  public Pf4jPluginBox pf4jPluginBox() throws IOException {
    return new Pf4jPluginManager(pluginsPath,
        pluginsTempPath,
        pluginsResourcesPath,
        pluginLoader,
        integrationTypeRepository,
        pluginManager(),
        context,
        applicationEventPublisher
    );
  }

  @Bean
  public PluginManager pluginManager() {

    return new DefaultPluginManager(Paths.get(pluginsPath)) {
      @Override
      protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return pluginDescriptorFinder();
      }

      @Override
      protected ExtensionFactory createExtensionFactory() {
        return new ReportPortalExtensionFactory(pluginsResourcesPath, this, context);
      }

      @Override
      protected ExtensionFinder createExtensionFinder() {
        RpExtensionFinder extensionFinder = new RpExtensionFinder(this);
        addPluginStateListener(extensionFinder);
        return extensionFinder;
      }

      class RpExtensionFinder extends DefaultExtensionFinder {

        private RpExtensionFinder(PluginManager pluginManager) {
          super(pluginManager);
          finders.clear();
          finders.add(new LegacyExtensionFinder(pluginManager) {
            @Override
            public Set<String> findClassNames(String pluginId) {
              return ofNullable(super.findClassNames(pluginId)).orElseGet(Collections::emptySet);
            }
          });
        }
      }
    };
  }

  @Bean
  public PluginDescriptorFinder pluginDescriptorFinder() {
    return new DetailManifestPluginDescriptorFinder();
  }

  @Bean
  public FileTypeMap fileTypeMap() {
    return new ConfigurableMimeFileTypeMap();
  }

  @Bean
  public PluginFilesProvider pluginPublicFilesProvider() {
    return new PluginFilesProvider(pluginsResourcesPath, publicFolderQualifier, fileTypeMap(),
        integrationTypeRepository);
  }

  @Bean
  public PluginFilesProvider pluginFilesProvider() {
    return new PluginFilesProvider(pluginsResourcesPath, "", fileTypeMap(),
        integrationTypeRepository);
  }

}
