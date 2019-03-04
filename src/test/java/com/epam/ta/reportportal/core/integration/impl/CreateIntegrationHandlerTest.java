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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CreateIntegrationHandlerTest {

	private final Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping = mock(Map.class);

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final MessageBus messageBus = mock(MessageBus.class);
	private final IntegrationService integrationService = mock(IntegrationService.class);

	private final CreateIntegrationHandler createIntegrationHandler = new CreateIntegrationHandlerImpl(integrationServiceMapping,
			integrationRepository,
			projectRepository,
			messageBus
	);

	@Test
	void createGlobalIntegration() {

		//given
		final UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setEnabled(true);
		updateIntegrationRQ.setIntegrationName(ReportPortalIntegrationEnum.EMAIL.name());
		updateIntegrationRQ.setIntegrationParams(IntegrationTestUtil.getParams());

		final long emailIntegrationId = 1L;

		//when
		when(integrationServiceMapping.get(ReportPortalIntegrationEnum.EMAIL)).thenReturn(integrationService);

		when(integrationService.createGlobalIntegration(updateIntegrationRQ.getIntegrationName(),
				updateIntegrationRQ.getIntegrationParams()
		)).thenReturn(IntegrationTestUtil.getGlobalEmailIntegration(emailIntegrationId));

		//then
		OperationCompletionRS operationCompletionRS = createIntegrationHandler.createGlobalIntegration(updateIntegrationRQ);

		assertNotNull(operationCompletionRS);
		assertEquals("Integration with id = " + emailIntegrationId + " has been successfully created.",
				operationCompletionRS.getResultMessage()
		);
	}

	@Test
	void createProjectIntegration() {

		//given
		final UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setEnabled(true);
		updateIntegrationRQ.setIntegrationName(ReportPortalIntegrationEnum.EMAIL.name());
		updateIntegrationRQ.setIntegrationParams(IntegrationTestUtil.getParams());

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		//when
		when(projectRepository.findById(projectId)).thenReturn(IntegrationTestUtil.getProjectWithId(projectId));

		when(integrationServiceMapping.get(ReportPortalIntegrationEnum.EMAIL)).thenReturn(integrationService);

		when(integrationService.createProjectIntegration(updateIntegrationRQ.getIntegrationName(),
				ProjectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME),
				updateIntegrationRQ.getIntegrationParams()
		)).thenReturn(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId, projectId));

		//then
		OperationCompletionRS operationCompletionRS = createIntegrationHandler.createProjectIntegration(ProjectExtractor.extractProjectDetails(user,
				TEST_PROJECT_NAME
		), updateIntegrationRQ, user);

		assertNotNull(operationCompletionRS);
		assertEquals("Integration with id = " + emailIntegrationId + " has been successfully created.",
				operationCompletionRS.getResultMessage()
		);

	}

	@Test
	void updateGlobalIntegration() {

		//given
		final UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setEnabled(true);
		updateIntegrationRQ.setIntegrationName(ReportPortalIntegrationEnum.EMAIL.name());
		updateIntegrationRQ.setIntegrationParams(IntegrationTestUtil.getParams());

		final long emailIntegrationId = 1L;

		//when
		when(integrationServiceMapping.get(ReportPortalIntegrationEnum.EMAIL)).thenReturn(integrationService);

		when(integrationService.updateGlobalIntegration(1L,
				updateIntegrationRQ.getIntegrationParams()
		)).thenReturn(IntegrationTestUtil.getGlobalEmailIntegration(emailIntegrationId));

		//then
		OperationCompletionRS operationCompletionRS = createIntegrationHandler.updateGlobalIntegration(emailIntegrationId, updateIntegrationRQ);

		assertNotNull(operationCompletionRS);
		assertEquals("Integration with id = " + emailIntegrationId + " has been successfully updated.",
				operationCompletionRS.getResultMessage()
		);
	}

	@Test
	void updateProjectIntegration() {

		//given
		final UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setEnabled(true);
		updateIntegrationRQ.setIntegrationName(ReportPortalIntegrationEnum.EMAIL.name());
		updateIntegrationRQ.setIntegrationParams(IntegrationTestUtil.getParams());

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		//when
		when(projectRepository.findById(projectId)).thenReturn(IntegrationTestUtil.getProjectWithId(projectId));

		when(integrationServiceMapping.get(ReportPortalIntegrationEnum.EMAIL)).thenReturn(integrationService);

		when(integrationService.updateProjectIntegration(emailIntegrationId,
				ProjectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME),
				updateIntegrationRQ.getIntegrationParams()
		)).thenReturn(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId, projectId));

		//then
		OperationCompletionRS operationCompletionRS = createIntegrationHandler.updateProjectIntegration(emailIntegrationId, ProjectExtractor.extractProjectDetails(user,
				TEST_PROJECT_NAME
		), updateIntegrationRQ, user);

		assertNotNull(operationCompletionRS);
		assertEquals("Integration with id = " + emailIntegrationId + " has been successfully updated.",
				operationCompletionRS.getResultMessage()
		);

	}
}