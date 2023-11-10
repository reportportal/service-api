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

package com.epam.ta.reportportal.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 **/
class CleanOutdatedPluginsJobTest {

  private final IntegrationTypeRepository integrationTypeRepository = mock(
      IntegrationTypeRepository.class);

  private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

  private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);

  private final CleanOutdatedPluginsJob cleanOutdatedPluginsJob = new CleanOutdatedPluginsJob(
      integrationTypeRepository,
      pluginBox
  );

  @Test
  void shouldDeleteIfNotFoundAndUnloadIfDisabled() {

    when(integrationTypeRepository.findAll()).thenReturn(getDisabledPlugins());
    when(pluginBox.getPlugins()).thenReturn(getPlugins());
    when(pluginBox.getPluginById(anyString())).thenReturn(Optional.of(pluginWrapper));

    cleanOutdatedPluginsJob.execute();

    verify(pluginBox, times(1)).deletePlugin(any(PluginWrapper.class));
    verify(pluginBox, times(2)).unloadPlugin(any(PluginWrapper.class));
  }

  private List<IntegrationType> getDisabledPlugins() {

    IntegrationType jira = new IntegrationType();
    jira.setName("jira");
    jira.setDetails(new IntegrationTypeDetails());

    IntegrationType rally = new IntegrationType();
    rally.setName("rally");

    IntegrationType random = new IntegrationType();
    rally.setName("random");

    return Lists.newArrayList(jira, rally, random);
  }

  private List<Plugin> getPlugins() {
    return Lists.newArrayList(new Plugin("jira", ExtensionPoint.BTS),
        new Plugin("rally", ExtensionPoint.BTS));
  }

}