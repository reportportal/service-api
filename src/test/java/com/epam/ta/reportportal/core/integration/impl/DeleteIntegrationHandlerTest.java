/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DeleteIntegrationHandlerTest {

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private final MessageBus messageBus = mock(MessageBus.class);

	private final DeleteIntegrationHandler deleteIntegrationHandler = new DeleteIntegrationHandlerImpl(integrationRepository,
			integrationTypeRepository,
			messageBus
	);

	@Test
	void deleteGlobalIntegration() {

		final long emailIntegrationId = 1L;

		when(integrationRepository.findGlobalById(emailIntegrationId)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(
				emailIntegrationId)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteGlobalIntegration(emailIntegrationId);

		assertNotNull(operationCompletionRS);
		assertEquals(
				Suppliers.formattedSupplier("Global integration with id = {} has been successfully removed", emailIntegrationId).get(),
				operationCompletionRS.getResultMessage()
		);

	}

	@Test
	void deleteAllIntegrations() {

		when(integrationTypeRepository.findByName(anyString())).thenReturn(Optional.ofNullable(IntegrationTestUtil.getEmailIntegrationType()));

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteGlobalIntegrationsByType("EMAIL");
		verify(integrationRepository, times(1)).deleteAllGlobalByIntegrationTypeId(anyLong());

		assertNotNull(operationCompletionRS);
		assertEquals("All global integrations with type ='EMAIL' integrations have been successfully removed.", operationCompletionRS.getResultMessage());
	}

	@Test
	void deleteProjectIntegration() {

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		when(integrationRepository.findByIdAndProjectId(emailIntegrationId,
				projectId
		)).thenReturn(Optional.of(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId, projectId)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegration(emailIntegrationId,
				ProjectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME),
				user
		);

		assertNotNull(operationCompletionRS);
		assertEquals("Integration with ID = '" + emailIntegrationId + "' has been successfully deleted.",
				operationCompletionRS.getResultMessage()
		);
	}

	@Test
	void deleteProjectIntegrations() {

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		when(integrationRepository.findAllByProjectId(projectId)).thenReturn(Lists.newArrayList(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId,
				projectId
		)));

		when(integrationTypeRepository.findByName(anyString())).thenReturn(Optional.ofNullable(IntegrationTestUtil.getEmailIntegrationType()));


		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegrationsByType("EMAIL", ProjectExtractor.extractProjectDetails(user,
				TEST_PROJECT_NAME
		), user);
		verify(integrationRepository, times(1)).deleteAllByProjectIdAndIntegrationTypeId(anyLong(), anyLong());

		assertNotNull(operationCompletionRS);
		assertEquals("All integrations with type ='1' for project with id ='1' have been successfully deleted",
				operationCompletionRS.getResultMessage()
		);

	}

}