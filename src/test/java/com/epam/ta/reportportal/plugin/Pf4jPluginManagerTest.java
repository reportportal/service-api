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

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Plugin;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginException;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class Pf4jPluginManagerTest {

	public static final String PLUGINS_PATH = "plugins";
	public static final String PLUGINS_TEMP_PATH = "plugins/temp";
	public static final String NEW_PLUGIN_FILE_NAME = "plugin.jar";
	public static final String NEW_PLUGIN_ID = "jira";

	private final PluginLoader pluginLoader = mock(PluginLoader.class);
	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
	private final PluginManager pluginManager = mock(PluginManager.class);
	private final PluginWrapper previousPlugin = mock(PluginWrapper.class);
	private final PluginWrapper newPlugin = mock(PluginWrapper.class);

	private final Pf4jPluginManager pluginBox = new Pf4jPluginManager(PLUGINS_PATH,
			PLUGINS_TEMP_PATH,
			pluginLoader,
			integrationTypeRepository,
			pluginManager,
			beanFactory
	);

	private final InputStream fileStream = mock(InputStream.class);

	Pf4jPluginManagerTest() throws IOException {
	}

	@AfterEach
	void cleanUp() throws IOException {
		File directory = new File("plugins");
		if (directory.exists()) {
			FileUtils.deleteDirectory(directory);
		}
	}

	@Test
	void uploadPlugin() throws PluginException, IOException {

		PluginInfo pluginInfo = getPluginInfo();
		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(pluginInfo);
		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(pluginLoader.retrieveIntegrationType(pluginInfo)).thenReturn(jiraIntegrationType);
		when(pluginManager.getPlugin(pluginInfo.getId())).thenReturn(null);
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(NEW_PLUGIN_ID);
		when(pluginManager.getPlugin(NEW_PLUGIN_ID)).thenReturn(newPlugin);
		when(pluginManager.getPluginsRoot()).thenReturn(FileSystems.getDefault().getPath(PLUGINS_PATH));
		when(pluginLoader.validatePluginExtensionClasses(newPlugin)).thenReturn(true);
		doNothing().when(pluginLoader).savePlugin(Paths.get(PLUGINS_PATH, NEW_PLUGIN_FILE_NAME), fileStream);
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(NEW_PLUGIN_ID);
		when(integrationTypeRepository.save(any(IntegrationType.class))).thenReturn(jiraIntegrationType);
		Path file = Files.createFile(Paths.get(PLUGINS_TEMP_PATH, "plugin.jar"));
		IntegrationType newIntegrationType = pluginBox.uploadPlugin(NEW_PLUGIN_FILE_NAME, fileStream);
		assertEquals(1L, newIntegrationType.getId().longValue());
	}

	@Test
	void uploadPluginWithExistingPlugin() throws PluginException, IOException {

		File tempFile = File.createTempFile(NEW_PLUGIN_FILE_NAME, ".jar", new File(PLUGINS_PATH));
		tempFile.deleteOnExit();
		PluginInfo pluginInfo = getPluginInfo();
		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, tempFile.getName()))).thenReturn(pluginInfo);
		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(pluginLoader.retrieveIntegrationType(pluginInfo)).thenReturn(jiraIntegrationType);
		when(pluginManager.getPlugin(pluginInfo.getId())).thenReturn(previousPlugin);
		when(pluginManager.getPluginsRoot()).thenReturn(FileSystems.getDefault().getPath(PLUGINS_PATH));
		when(previousPlugin.getPluginPath()).thenReturn(Paths.get("another/path"));
		when(pluginManager.unloadPlugin(any())).thenReturn(true);
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> pluginBox.uploadPlugin(tempFile.getName(), fileStream)
		);
		assertEquals(
				"Error during plugin uploading: 'Unable to rewrite plugin file = '" + tempFile.getName() + "' with different plugin type'",
				exception.getMessage()
		);
	}

	@Test
	void uploadPluginWithExistingFile() throws PluginException, IOException {
		File tempFile = File.createTempFile(NEW_PLUGIN_FILE_NAME, ".jar", new File(PLUGINS_TEMP_PATH));
		tempFile.deleteOnExit();
		PluginInfo pluginInfo = getPluginInfo();
		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, tempFile.getName()))).thenReturn(pluginInfo);
		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(pluginLoader.retrieveIntegrationType(pluginInfo)).thenReturn(jiraIntegrationType);
		when(pluginManager.getPlugin(pluginInfo.getId())).thenReturn(null);
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_TEMP_PATH, tempFile.getName()))).thenReturn(NEW_PLUGIN_ID);
		when(pluginManager.getPlugin(NEW_PLUGIN_ID)).thenReturn(newPlugin);
		when(pluginManager.getPluginsRoot()).thenReturn(FileSystems.getDefault().getPath(PLUGINS_PATH));
		when(pluginLoader.validatePluginExtensionClasses(newPlugin)).thenReturn(true);
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_PATH, tempFile.getName()))).thenReturn(NEW_PLUGIN_ID);
		when(integrationTypeRepository.save(any(IntegrationType.class))).thenReturn(jiraIntegrationType);
		IntegrationType newIntegrationType = pluginBox.uploadPlugin(tempFile.getName(), fileStream);
		assertEquals(1L, newIntegrationType.getId().longValue());
	}

	@Test
	void uploadPluginWithLoadingError() throws PluginException {

		PluginInfo pluginInfo = getPluginInfo();
		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(pluginInfo);
		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(pluginLoader.retrieveIntegrationType(pluginInfo)).thenReturn(jiraIntegrationType);
		when(pluginManager.getPlugin(pluginInfo.getId())).thenReturn(null);
		when(previousPlugin.getPluginState()).thenReturn(PluginState.STARTED);
		when(pluginManager.getPluginsRoot()).thenReturn(FileSystems.getDefault().getPath(PLUGINS_PATH));
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(null);
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> pluginBox.uploadPlugin(NEW_PLUGIN_FILE_NAME, fileStream)
		);
		assertEquals("Error during plugin uploading: 'Failed to load new plugin from file = plugin.jar'", exception.getMessage());
	}

	@Test
	void uploadPluginWithoutExtensionClasses() throws PluginException {

		PluginInfo pluginInfo = getPluginInfo();
		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(pluginInfo);
		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(pluginLoader.retrieveIntegrationType(pluginInfo)).thenReturn(jiraIntegrationType);
		when(pluginManager.getPlugin(pluginInfo.getId())).thenReturn(null);
		when(pluginManager.loadPlugin(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(NEW_PLUGIN_ID);
		when(pluginManager.getPlugin(NEW_PLUGIN_ID)).thenReturn(newPlugin);
		when(pluginManager.getPluginsRoot()).thenReturn(FileSystems.getDefault().getPath(PLUGINS_PATH));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> pluginBox.uploadPlugin(NEW_PLUGIN_FILE_NAME, fileStream)
		);
		assertEquals("Error during plugin uploading: 'New plugin with id = jira doesn't have mandatory extension classes.'",
				exception.getMessage()
		);
	}

	@Test
	void uploadPluginWithPluginException() throws PluginException {

		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenThrow(new PluginException(
				"Manifest not found"));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> pluginBox.uploadPlugin(NEW_PLUGIN_FILE_NAME, fileStream)
		);
		assertEquals("Error during plugin uploading: 'Manifest not found'", exception.getMessage());
	}

	@Test
	void uploadPluginWithoutVersion() throws PluginException {

		when(pluginLoader.extractPluginInfo(Paths.get(PLUGINS_TEMP_PATH, NEW_PLUGIN_FILE_NAME))).thenReturn(getPluginInfoWithoutVersion());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> pluginBox.uploadPlugin(NEW_PLUGIN_FILE_NAME, fileStream)
		);
		assertEquals("Error during plugin uploading: 'Plugin version should be specified.'", exception.getMessage());
	}

	@Test
	void getPlugins() {
		when(pluginManager.getPlugins()).thenReturn(Lists.newArrayList(newPlugin));
		when(newPlugin.getPluginId()).thenReturn(NEW_PLUGIN_ID);
		when(pluginManager.getExtensionClasses(NEW_PLUGIN_ID)).thenReturn(Lists.newArrayList(BtsExtension.class));
		List<Plugin> plugins = pluginBox.getPlugins();
		assertNotNull(plugins);
		assertEquals(1L, plugins.size());
	}

	private PluginInfo getPluginInfo() {
		return new PluginInfo("old_jira", "1.0");
	}

	private PluginInfo getPluginInfoWithoutVersion() {
		return new PluginInfo("jira", null);
	}
}