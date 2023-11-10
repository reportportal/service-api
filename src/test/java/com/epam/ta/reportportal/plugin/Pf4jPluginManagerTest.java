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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class Pf4jPluginManagerTest {

  public static final String NEW_JIRA_PLUGIN_ID = "new_jira";

  private final PluginWrapper newPlugin = mock(PluginWrapper.class);


  private final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
  private final PluginManager pluginManager = mock(PluginManager.class);
  private final ApplicationEventPublisher applicationEventPublisher = mock(
      ApplicationEventPublisher.class);

  private final Pf4jPluginManager pluginBox = new Pf4jPluginManager(pluginManager, beanFactory,
      applicationEventPublisher);

  Pf4jPluginManagerTest() {
  }

  @Test
  void getPlugins() {
    when(pluginManager.getPlugins()).thenReturn(Lists.newArrayList(newPlugin));
    when(newPlugin.getPluginId()).thenReturn(NEW_JIRA_PLUGIN_ID);
    when(pluginManager.getExtensionClasses(NEW_JIRA_PLUGIN_ID)).thenReturn(
        Lists.newArrayList(BtsExtension.class));
    List<Plugin> plugins = pluginBox.getPlugins();
    assertNotNull(plugins);
    assertEquals(1L, plugins.size());
  }

}