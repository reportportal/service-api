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

import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.job.service.PluginLoaderService;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LoadPluginsJobTest {

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);

	private PluginLoaderService pluginLoaderService = mock(PluginLoaderService.class);

	private String pluginsRootPath = "plugins";

	private Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private DataStore dataStore = mock(DataStore.class);

	private PluginWrapper rallyPlugin = mock(PluginWrapper.class);
	private IntegrationType rally = mock(IntegrationType.class);

	private LoadPluginsJob loadPluginsJob = new LoadPluginsJob(pluginsRootPath,
			integrationTypeRepository,
			pluginLoaderService,
			pluginBox,
			dataStore
	);

	@AfterAll
	public static void clearPluginDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/plugins"));
	}

	@Test
	void loadDisabledPluginTest() throws IOException {

		List<IntegrationType> integrationTypes = getIntegrationTypes();
		when(integrationTypeRepository.findAll()).thenReturn(integrationTypes);
		List<PluginInfo> pluginInfos = getPluginInfos();

		File tempFile = File.createTempFile("file", ".jar");
		tempFile.deleteOnExit();

		when(dataStore.load(any(String.class))).thenReturn(new FileInputStream(tempFile));
		when(pluginBox.loadPlugin(any(String.class), any(IntegrationTypeDetails.class))).thenReturn(true);
		when(pluginLoaderService.getNotLoadedPluginsInfo()).thenReturn(pluginInfos);
		when(pluginBox.getPluginById(any(String.class))).thenReturn(java.util.Optional.ofNullable(rallyPlugin));
		when(rallyPlugin.getPluginId()).thenReturn("rally");
		when(integrationTypeRepository.findByName(any(String.class))).thenReturn(java.util.Optional.ofNullable(rally));
		when(pluginBox.unloadPlugin(rally)).thenReturn(true);

		loadPluginsJob.execute();
	}

	private List<IntegrationType> getIntegrationTypes() {
		IntegrationType jira = new IntegrationType();
		jira.setName("jira");
		IntegrationType rally = new IntegrationType();
		rally.setName("rally");
		return Lists.newArrayList(jira, rally);
	}

	private List<PluginInfo> getPluginInfos() {

		return Lists.newArrayList(new PluginInfo("jira", "v1.0", "file Id", "jira file", true),
				new PluginInfo("rally", "v2.0", "file Id", "rally file", false)
		);
	}

}