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
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DeleteIntegrationHandlerTest {

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final MessageBus messageBus = mock(MessageBus.class);

	private final DeleteIntegrationHandler deleteIntegrationHandler = new DeleteIntegrationHandlerImpl(integrationRepository, messageBus);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void deleteGlobalIntegration() {

		final long emailIntegrationId = 1L;

		when(integrationRepository.findGlobalById(emailIntegrationId)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(
				emailIntegrationId)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteGlobalIntegration(emailIntegrationId);

		Assert.assertNotNull(operationCompletionRS);
		Assert.assertEquals(
				Suppliers.formattedSupplier("Global integration with id = {} has been successfully removed", emailIntegrationId).get(),
				operationCompletionRS.getResultMessage()
		);

	}

	@Test
	public void deleteAllIntegrations() {

		doNothing().when(integrationRepository).deleteAllInBatch();

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteAllIntegrations();

		Assert.assertNotNull(operationCompletionRS);
		Assert.assertEquals("All integrations have been successfully removed.", operationCompletionRS.getResultMessage());
	}

	@Test
	public void deleteProjectIntegration() {

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		when(integrationRepository.findByIdAndProjectId(emailIntegrationId,
				projectId
		)).thenReturn(Optional.of(IntegrationTestUtil.getProjectEmailIntegration(
				emailIntegrationId,
				projectId
		)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegration(emailIntegrationId,
				ProjectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME),
				user
		);

		Assert.assertNotNull(operationCompletionRS);
		Assert.assertEquals("Integration with ID = '" + emailIntegrationId + "' has been successfully deleted.",
				operationCompletionRS.getResultMessage()
		);
	}

	@Test
	public void deleteProjectIntegrations() {

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

		doNothing().when(integrationRepository).deleteAllInBatch();

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegrations(ProjectExtractor.extractProjectDetails(user,
				TEST_PROJECT_NAME
		), user);

		Assert.assertNotNull(operationCompletionRS);
		Assert.assertEquals(
				"All integrations for project with id ='" + projectId + "' have been successfully deleted",
				operationCompletionRS.getResultMessage()
		);

	}

}