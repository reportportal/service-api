/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CreatePluginHandlerTest {

	public static final String PLUGIN_ID = "jira";
	public static final String WRONG_PLUGIN_ID = "JERA";
	public static final String PLUGIN_VERSION = "1.0.0";
	public static final String FILE_NAME = "file.jar";

	private final String pluginRootPath = "plugins";

	private final PluginInfo pluginInfo = mock(PluginInfo.class);

	private final MultipartFile multipartFile = mock(MultipartFile.class);

	private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private final PluginLoader pluginLoader = mock(PluginLoader.class);

	private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);

	private final InputStream inputStream = mock(InputStream.class);

	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);

	private final DataStore dataStore = mock(DataStore.class);

	private final CreatePluginHandler createPluginHandler = new CreatePluginHandlerImpl(pluginRootPath,
			pluginBox,
			pluginLoader,
			integrationTypeRepository,
			dataStore
	);

	@Test
	void shouldUploadPluginWhenValid() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginLoader.extractPluginInfo(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(pluginInfo);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

		when(pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME)).thenReturn(Optional.of(pluginWrapper));

		when(pluginBox.loadPlugin(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		when(pluginLoader.validatePluginExtensionClasses(PLUGIN_ID)).thenReturn(true);

		when(pluginBox.unloadPlugin(PLUGIN_ID)).thenReturn(true);

		doNothing().when(pluginLoader).deletePreviousPlugin(pluginWrapper, FILE_NAME);

		when(pluginBox.loadPlugin(Paths.get(pluginRootPath, FILE_NAME))).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		when(integrationTypeRepository.save(any(IntegrationType.class))).thenReturn(IntegrationTestUtil.getJiraIntegrationType());

		EntryCreatedRS entryCreatedRS = createPluginHandler.uploadPlugin(multipartFile);

		assertNotNull(entryCreatedRS);
		assertEquals(IntegrationTestUtil.getJiraIntegrationType().getId(), entryCreatedRS.getId());
	}

	@Test
	void shouldNotUploadPluginWhenExtensionsAreNotValid() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginLoader.extractPluginInfo(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(pluginInfo);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

		when(pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME)).thenReturn(Optional.of(pluginWrapper));

		when(pluginBox.loadPlugin(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		when(pluginLoader.validatePluginExtensionClasses(PLUGIN_ID)).thenReturn(false);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> createPluginHandler.uploadPlugin(multipartFile)
		);

		assertEquals(
				Suppliers.formattedSupplier("Error during plugin uploading: 'New plugin with id = {} doesn't have mandatory extension classes.'",
						PLUGIN_ID
				).get(),
				exception.getMessage()
		);
	}

	@Test
	void shouldNotCreatePluginWhenCannotSaveFile() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginLoader.extractPluginInfo(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(pluginInfo);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

		when(pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME)).thenReturn(Optional.of(pluginWrapper));

		when(pluginBox.loadPlugin(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		when(pluginLoader.validatePluginExtensionClasses(PLUGIN_ID)).thenReturn(true);

		when(pluginBox.unloadPlugin(PLUGIN_ID)).thenReturn(true);

		when(multipartFile.getInputStream()).thenThrow(IOException.class);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> createPluginHandler.uploadPlugin(multipartFile)
		);

		assertEquals(
				Suppliers.formattedSupplier(
						"Error during plugin uploading: 'Unable to upload the new plugin file with id = jira to the data store'").get(),
				exception.getMessage()
		);
	}

	@Test
	void shouldNotLoadWhenNoVersionProvided() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginLoader.extractPluginInfo(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(pluginInfo);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(null);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> createPluginHandler.uploadPlugin(multipartFile)
		);

		assertEquals("Impossible interact with integration. Plugin version should be specified.", exception.getMessage());
	}

	@Test
	void shouldNotCreateWhenNotLoaded() throws IOException {

		when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);

		when(multipartFile.getInputStream()).thenReturn(inputStream);

		when(pluginLoader.extractPluginInfo(Paths.get(pluginRootPath,
				CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY,
				FILE_NAME
		))).thenReturn(pluginInfo);

		when(pluginInfo.getId()).thenReturn(PLUGIN_ID);
		when(pluginInfo.getVersion()).thenReturn(PLUGIN_VERSION);

		when(pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME)).thenReturn(Optional.of(pluginWrapper));

		when(pluginBox.loadPlugin(Paths.get(pluginRootPath, CreatePluginHandlerImpl.PLUGIN_TEMP_DIRECTORY, FILE_NAME))).thenReturn(null);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> createPluginHandler.uploadPlugin(multipartFile)
		);

		assertEquals(
				Suppliers.formattedSupplier("Error during plugin uploading: 'Failed to load new plugin from file = {}'", FILE_NAME).get(),
				exception.getMessage()
		);
	}
}