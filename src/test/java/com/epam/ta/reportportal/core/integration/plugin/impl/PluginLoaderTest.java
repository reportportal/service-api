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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.plugin.DetailPluginDescriptor;
import com.epam.ta.reportportal.util.FeatureFlagHandler;
import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PluginLoaderTest {

  private static final String PLUGIN_ID = "pluginV1";
  private static final String PLUGIN_VERSION = "1.0.0";
  private static final String FILE_NAME = "file.jar";

  private final DataStore dataStore = mock(DataStore.class);

  private final IntegrationTypeRepository integrationTypeRepository =
      mock(IntegrationTypeRepository.class);

  private final PluginDescriptorFinder pluginDescriptorFinder = mock(PluginDescriptorFinder.class);

  private final DetailPluginDescriptor pluginDescriptor = mock(DetailPluginDescriptor.class);

  private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);

  private final PluginManager pluginManager = mock(PluginManager.class);

  private final PluginInfo pluginInfo = mock(PluginInfo.class);

  private final FeatureFlagHandler featureFlagHandler = mock(FeatureFlagHandler.class);

  private final PluginLoader pluginLoader =
      new PluginLoaderImpl(dataStore, integrationTypeRepository, pluginDescriptorFinder,
          featureFlagHandler
      );

  @Test
  void shouldExtractPluginIdWhenExists() throws PluginRuntimeException {

    Path path = Paths.get("dir", FILE_NAME);

    when(pluginDescriptorFinder.find(path)).thenReturn(pluginDescriptor);
    when(pluginDescriptor.getPluginId()).thenReturn(PLUGIN_ID);
    when(pluginDescriptor.getVersion()).thenReturn(PLUGIN_VERSION);

    PluginInfo pluginInfo = pluginLoader.extractPluginInfo(path);

    assertNotNull(pluginInfo);
    assertEquals(PLUGIN_ID, pluginInfo.getId());
    assertEquals(PLUGIN_VERSION, pluginInfo.getVersion());
  }

  @Test
  void shouldReturnTrueWhenPluginExtensionClassesValid() {

    when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);
    when(pluginWrapper.getPluginId()).thenReturn(PLUGIN_ID);
    when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(
        Lists.newArrayList(BtsExtension.class));

    boolean isValid = pluginLoader.validatePluginExtensionClasses(pluginWrapper);

    assertTrue(isValid);
  }

  @Test
  void shouldReturnFalseWhenPluginExtensionClassesInvalid() {

    when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);
    when(pluginWrapper.getPluginId()).thenReturn(PLUGIN_ID);
    when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(
        Lists.newArrayList(Collection.class));

    boolean isValid = pluginLoader.validatePluginExtensionClasses(pluginWrapper);

    assertFalse(isValid);
  }

  @Test
  void retrieveIntegrationTypeTest() {
    pluginLoader.resolvePluginDetails(pluginInfo);
  }
}