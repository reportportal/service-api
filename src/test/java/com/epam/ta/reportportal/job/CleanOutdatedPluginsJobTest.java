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

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 **/
class CleanOutdatedPluginsJobTest {

	public static final String PLUGIN_TEMP_DIRECTORY = "/temp/";

	private String pluginsRootPath = "plugins";

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);

	private Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private PluginLoaderService pluginLoaderService = mock(PluginLoaderService.class);

	private PluginWrapper jiraPlugin = mock(PluginWrapper.class);
	private IntegrationType jiraIntegrationType = mock(IntegrationType.class);
	private PluginWrapper rallyPlugin = mock(PluginWrapper.class);
	private IntegrationType rallyIntegrationType = mock(IntegrationType.class);

	private CleanOutdatedPluginsJob cleanOutdatedPluginsJob = new CleanOutdatedPluginsJob(pluginsRootPath + PLUGIN_TEMP_DIRECTORY,
			integrationTypeRepository,
			pluginBox,
			pluginLoaderService
	);

	@Test
	void testExecutionWithoutPluginInCache() throws IOException {

		File dir = new File(pluginsRootPath + PLUGIN_TEMP_DIRECTORY);
		dir.mkdirs();

		File file = new File(dir, "qwe.jar");

		file.createNewFile();

		when(pluginBox.isInUploadingState(any(String.class))).thenReturn(false);

		cleanOutdatedPluginsJob.execute();
	}

	@Test
	void testExecutionWithPluginInCache() throws IOException {

		File dir = new File(pluginsRootPath + PLUGIN_TEMP_DIRECTORY);
		dir.mkdirs();

		File file = File.createTempFile("test", ".jar", dir);

		file.deleteOnExit();

		when(pluginBox.isInUploadingState(any(String.class))).thenReturn(true);

		cleanOutdatedPluginsJob.execute();
	}

	@Test
	void testBrokenIntegrationTypeRemoving() {

		when(integrationTypeRepository.findAll()).thenReturn(getBrokenIntegrationType());

		cleanOutdatedPluginsJob.execute();
		verify(pluginLoaderService, times(2)).checkAndDeleteIntegrationType(any(IntegrationType.class));
	}

	@Test
	void testTemporaryPluginRemoving() {
		List<Plugin> plugins = getPlugins();
		when(integrationTypeRepository.findAll()).thenReturn(Collections.emptyList());

		when(pluginBox.getPlugins()).thenReturn(plugins);
		when(pluginBox.getPluginById(plugins.get(0).getId())).thenReturn(ofNullable(jiraPlugin));
		when(jiraPlugin.getPluginPath()).thenReturn(Paths.get(pluginsRootPath, "qwe.jar"));

		when(pluginBox.isInUploadingState(jiraPlugin.getPluginPath().getFileName().toString())).thenReturn(false);
		when(integrationTypeRepository.findByName(jiraPlugin.getPluginId())).thenReturn(Optional.of(jiraIntegrationType));
		when(pluginBox.unloadPlugin(jiraIntegrationType)).thenReturn(true);

		when(pluginBox.getPluginById(plugins.get(1).getId())).thenReturn(ofNullable(rallyPlugin));
		when(rallyPlugin.getPluginPath()).thenReturn(Paths.get(pluginsRootPath, "qwe1.jar"));

		when(pluginBox.isInUploadingState(rallyPlugin.getPluginPath().getFileName().toString())).thenReturn(false);
		when(integrationTypeRepository.findByName(rallyPlugin.getPluginId())).thenReturn(Optional.of(rallyIntegrationType));
		when(pluginBox.unloadPlugin(rallyIntegrationType)).thenReturn(false);

		cleanOutdatedPluginsJob.execute();

	}

	private List<IntegrationType> getBrokenIntegrationType() {

		IntegrationType jira = new IntegrationType();
		jira.setName("jira");
		jira.setDetails(new IntegrationTypeDetails());

		IntegrationType rally = new IntegrationType();
		rally.setName("rally");

		return Lists.newArrayList(jira, rally);
	}

	private List<Plugin> getPlugins() {
		return Lists.newArrayList(new Plugin("jira", ExtensionPoint.BTS), new Plugin("rally", ExtensionPoint.BTS));
	}

}