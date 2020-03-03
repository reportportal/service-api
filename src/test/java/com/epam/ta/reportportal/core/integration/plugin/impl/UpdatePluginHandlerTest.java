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

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdatePluginStateRQ;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class UpdatePluginHandlerTest {

	private static final String FILE_NAME = "file-name";
	private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);
	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private final DataStore dataStore = mock(DataStore.class);

	private PluginWrapper pluginWrapper = mock(PluginWrapper.class);

	private final UpdatePluginHandler updatePluginHandler = new UpdatePluginHandlerImpl(pluginBox, integrationTypeRepository);

	@AfterAll
	static void clearPluginDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/plugins"));
	}

	@Test
	void shouldNotUpdatePluginIntegrationWhenNotExistsById() {

		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(true);

		when(integrationTypeRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> updatePluginHandler.updatePluginState(1L, updatePluginStateRQ)
		);

		assertEquals(Suppliers.formattedSupplier("Impossible interact with integration. Integration type with id - '{}' not found.", 1L)
				.get(), exception.getMessage());

	}

	@Test
	void shouldUpdateNotPluginIntegrationWhenExists() {

		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(true);

		IntegrationType emailIntegrationType = IntegrationTestUtil.getEmailIntegrationType();
		when(integrationTypeRepository.findById(1L)).thenReturn(Optional.of(emailIntegrationType));

		OperationCompletionRS operationCompletionRS = updatePluginHandler.updatePluginState(1L, updatePluginStateRQ);

		Assertions.assertEquals(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
				emailIntegrationType.getName(),
				updatePluginStateRQ.getEnabled()
		).get(), operationCompletionRS.getResultMessage());
	}

	@Test
	void shouldUnloadPluginWhenDisabledAndIsPresent() {
		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(false);

		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(integrationTypeRepository.findById(1L)).thenReturn(Optional.of(jiraIntegrationType));

		when(pluginBox.getPluginById(jiraIntegrationType.getName())).thenReturn(Optional.ofNullable(pluginWrapper));
		when(pluginWrapper.getPluginId()).thenReturn(jiraIntegrationType.getName());
		when(pluginBox.unloadPlugin(jiraIntegrationType)).thenReturn(true);
		OperationCompletionRS operationCompletionRS = updatePluginHandler.updatePluginState(1L, updatePluginStateRQ);

		Assertions.assertEquals(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
				jiraIntegrationType.getName(),
				updatePluginStateRQ.getEnabled()
		).get(), operationCompletionRS.getResultMessage());
	}

	@Test
	void shouldThrowWhenNotUnloaded() {
		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(false);

		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		when(integrationTypeRepository.findById(1L)).thenReturn(Optional.of(jiraIntegrationType));

		when(pluginBox.getPluginById(jiraIntegrationType.getName())).thenReturn(Optional.ofNullable(pluginWrapper));
		when(pluginWrapper.getPluginId()).thenReturn(jiraIntegrationType.getName());
		when(pluginBox.unloadPlugin(jiraIntegrationType)).thenReturn(false);
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> updatePluginHandler.updatePluginState(1L, updatePluginStateRQ)
		);

		assertEquals(Suppliers.formattedSupplier("Impossible interact with integration. Error during unloading the plugin with id = '{}'",
				jiraIntegrationType.getName()
		).get(), exception.getMessage());
	}

	@Test
	void shouldNotUpdatePluginIntegrationWhenReportPortalIntegrationNotExists() {

		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(true);

		IntegrationType emailIntegrationType = IntegrationTestUtil.getEmailIntegrationType();
		final String wrongIntegrationTypeName = "QWEQWE";
		emailIntegrationType.setName(wrongIntegrationTypeName);
		when(integrationTypeRepository.findById(1L)).thenReturn(Optional.of(emailIntegrationType));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> updatePluginHandler.updatePluginState(1L, updatePluginStateRQ)
		);

		assertEquals(Suppliers.formattedSupplier("Impossible interact with integration. Error during loading the plugin with id = 'QWEQWE'",
				wrongIntegrationTypeName
		).get(), exception.getMessage());

	}

	@Test
	void shouldLoadPluginWhenEnabledAndIsNotPresent() throws IOException {
		UpdatePluginStateRQ updatePluginStateRQ = new UpdatePluginStateRQ();
		updatePluginStateRQ.setEnabled(true);

		IntegrationType jiraIntegrationType = IntegrationTestUtil.getJiraIntegrationType();
		jiraIntegrationType.getDetails().setDetails(getCorrectJiraIntegrationDetailsParams());

		when(integrationTypeRepository.findById(1L)).thenReturn(ofNullable(jiraIntegrationType));
		when(pluginBox.getPluginById(jiraIntegrationType.getName())).thenReturn(Optional.empty());

		File tempFile = File.createTempFile("qwe", "txt");
		tempFile.deleteOnExit();

		when(dataStore.load(any(String.class))).thenReturn(new FileInputStream(tempFile));
		when(pluginBox.loadPlugin(jiraIntegrationType.getName(), jiraIntegrationType.getDetails())).thenReturn(true);
		OperationCompletionRS operationCompletionRS = updatePluginHandler.updatePluginState(1L, updatePluginStateRQ);

		Assertions.assertEquals(Suppliers.formattedSupplier("Enabled state of the plugin with id = '{}' has been switched to - '{}'",
				jiraIntegrationType.getName(),
				updatePluginStateRQ.getEnabled()
		).get(), operationCompletionRS.getResultMessage());
	}

	private Map<String, Object> getCorrectJiraIntegrationDetailsParams() {

		Map<String, Object> params = new HashMap<>();
		params.put(IntegrationTypeProperties.FILE_ID.getAttribute(), "file-id");
		params.put(IntegrationTypeProperties.FILE_NAME.getAttribute(), FILE_NAME);
		params.put(IntegrationTypeProperties.VERSION.getAttribute(), "1.0.0");
		return params;
	}

}
