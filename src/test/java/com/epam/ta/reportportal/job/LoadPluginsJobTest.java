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

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static com.epam.reportportal.extension.common.IntegrationTypeProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LoadPluginsJobTest {

  private final IntegrationTypeRepository integrationTypeRepository = mock(
      IntegrationTypeRepository.class);

  private final PluginLoader pluginLoader = mock(PluginLoader.class);

  private LoadPluginsJob loadPluginsJob = new LoadPluginsJob(integrationTypeRepository, pluginLoader);

  @Test
  void loadEnabledPluginTest() {

    List<IntegrationType> integrationTypes = getIntegrationTypes();
    when(integrationTypeRepository.findAll()).thenReturn(integrationTypes);

    loadPluginsJob.execute();

    final ArgumentCaptor<IntegrationType> integrationTypeArgumentCaptor = ArgumentCaptor.forClass(IntegrationType.class);
    verify(pluginLoader, times(1)).load(integrationTypeArgumentCaptor.capture());

    final IntegrationType capturedValue = integrationTypeArgumentCaptor.getValue();

    assertEquals("jira", capturedValue.getName());

  }

  private List<IntegrationType> getIntegrationTypes() {
    IntegrationType jira = new IntegrationType();
    jira.setName("jira");
    jira.setEnabled(true);
    final IntegrationTypeDetails details = new IntegrationTypeDetails();
    details.setDetails(Map.of(FILE_ID.getAttribute(), "fileId", VERSION.getAttribute(), "v1", FILE_NAME.getAttribute(), "file.jar"));
    jira.setDetails(details);
    IntegrationType rally = new IntegrationType();
    rally.setName("rally");
    return Lists.newArrayList(jira, rally);
  }

}