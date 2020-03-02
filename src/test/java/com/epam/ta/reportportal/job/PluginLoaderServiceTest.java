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

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.job.service.PluginLoaderService;
import com.epam.ta.reportportal.job.service.impl.PluginLoaderServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PluginLoaderServiceTest {

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);

	private Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private PluginLoaderService pluginLoaderService = new PluginLoaderServiceImpl(integrationTypeRepository, pluginBox);

	private PluginWrapper jiraPlugin = mock(PluginWrapper.class);
	private PluginWrapper rallyPlugin = mock(PluginWrapper.class);

	private PluginDescriptor jiraPluginDescriptor = mock(PluginDescriptor.class);
	private PluginDescriptor rallyPluginDescriptor = mock(PluginDescriptor.class);

	@Test
	void getNotLoadedPluginsInfoTest() {

		when(pluginBox.getPluginById("jira")).thenReturn(Optional.ofNullable(jiraPlugin));
		when(pluginBox.getPluginById("rally")).thenReturn(Optional.ofNullable(rallyPlugin));
		when(jiraPlugin.getDescriptor()).thenReturn(jiraPluginDescriptor);
		when(jiraPluginDescriptor.getVersion()).thenReturn("v1");
		when(rallyPlugin.getDescriptor()).thenReturn(rallyPluginDescriptor);
		when(rallyPluginDescriptor.getVersion()).thenReturn("another version");
		when(integrationTypeRepository.findAll()).thenReturn(getIntegrationTypes());
		List<PluginInfo> notLoadedPluginsInfo = pluginLoaderService.getNotLoadedPluginsInfo();

		Assertions.assertFalse(notLoadedPluginsInfo.isEmpty());
		Assertions.assertEquals(1, notLoadedPluginsInfo.size());
		Assertions.assertEquals("rally", notLoadedPluginsInfo.get(0).getId());
	}

	@Test
	void checkAndDeleteIntegrationTypeWhenPluginPositive() {
		IntegrationType integrationType = new IntegrationType();
		integrationType.setId(1L);
		integrationType.setName("jira");

		when(pluginBox.getPluginById(integrationType.getName())).thenReturn(Optional.ofNullable(jiraPlugin));
		when(jiraPlugin.getPluginId()).thenReturn("jira");
		when(jiraPlugin.getPluginPath()).thenReturn(Paths.get("plugins", "file.jar"));
		when(pluginBox.unloadPlugin(integrationType)).thenReturn(true);

		pluginLoaderService.checkAndDeleteIntegrationType(integrationType);

		verify(integrationTypeRepository, times(1)).deleteById(integrationType.getId());
	}

	@Test
	void checkAndDeleteIntegrationTypeWhenPluginNegative() {
		IntegrationType integrationType = new IntegrationType();
		integrationType.setId(1L);
		integrationType.setName("jira");

		when(pluginBox.getPluginById(integrationType.getName())).thenReturn(Optional.ofNullable(jiraPlugin));
		when(jiraPlugin.getPluginId()).thenReturn("jira");
		when(pluginBox.unloadPlugin(integrationType)).thenReturn(false);

		pluginLoaderService.checkAndDeleteIntegrationType(integrationType);

		verify(integrationTypeRepository, times(0)).deleteById(integrationType.getId());
	}

	@Test
	void checkAndDeleteIntegrationTypeWhenNotPluginTest() {
		IntegrationType integrationType = new IntegrationType();
		integrationType.setId(1L);
		integrationType.setName("EMAIL");

		pluginLoaderService.checkAndDeleteIntegrationType(integrationType);

		verify(integrationTypeRepository, times(0)).deleteById(integrationType.getId());
	}

	private List<IntegrationType> getIntegrationTypes() {

		IntegrationType jira = new IntegrationType();
		jira.setName("jira");
		IntegrationTypeDetails jiraDetails = new IntegrationTypeDetails();
		Map<String, Object> jiraParams = Maps.newHashMap();
		jiraParams.put(IntegrationTypeProperties.FILE_ID.getAttribute(), "f1");
		jiraParams.put(IntegrationTypeProperties.FILE_NAME.getAttribute(), "fname1");
		jiraParams.put(IntegrationTypeProperties.VERSION.getAttribute(), "v1");
		jiraParams.put(IntegrationTypeProperties.COMMANDS.getAttribute(), "");
		jiraDetails.setDetails(jiraParams);
		jira.setEnabled(true);
		jira.setDetails(jiraDetails);

		IntegrationType rally = new IntegrationType();
		rally.setEnabled(true);
		Map<String, Object> rallyParams = Maps.newHashMap();
		rallyParams.put(IntegrationTypeProperties.FILE_ID.getAttribute(), "f2");
		rallyParams.put(IntegrationTypeProperties.FILE_NAME.getAttribute(), "fname2");
		rallyParams.put(IntegrationTypeProperties.VERSION.getAttribute(), "v2");
		rallyParams.put(IntegrationTypeProperties.COMMANDS.getAttribute(), "");
		IntegrationTypeDetails rallyDetails = new IntegrationTypeDetails();
		rallyDetails.setDetails(rallyParams);
		rally.setName("rally");
		rally.setDetails(rallyDetails);

		IntegrationType noDetails = new IntegrationType();
		noDetails.setName("NO DETAILS");

		IntegrationType emptyParams = new IntegrationType();
		emptyParams.setName("EMPTY PARAMS");
		emptyParams.setDetails(new IntegrationTypeDetails());

		return Lists.newArrayList(jira, rally, noDetails, emptyParams);
	}

}