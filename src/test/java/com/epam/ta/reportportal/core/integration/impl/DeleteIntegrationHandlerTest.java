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

package com.epam.ta.reportportal.core.integration.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Lists;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DeleteIntegrationHandlerTest {

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

	private final DeleteIntegrationHandler deleteIntegrationHandler = new DeleteIntegrationHandlerImpl(integrationRepository,
			projectRepository,
			integrationTypeRepository,
			eventPublisher
	);

	@Test
	void deleteGlobalIntegration() {

    final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
        UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

		final long emailIntegrationId = 1L;

		when(integrationRepository.findGlobalById(emailIntegrationId)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(
				emailIntegrationId)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteGlobalIntegration(emailIntegrationId, user);

		assertNotNull(operationCompletionRS);
		assertEquals(
				Suppliers.formattedSupplier("Global integration with id = {} has been successfully removed", emailIntegrationId).get(),
				operationCompletionRS.getResultMessage()
		);

	}

	@Test
	void deleteAllIntegrations() {

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

		when(integrationTypeRepository.findByName(anyString())).thenReturn(
				Optional.of(IntegrationTestUtil.getEmailIntegrationType()));

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteGlobalIntegrationsByType(
				"EMAIL", user);
		verify(integrationRepository, times(1)).deleteAllGlobalByIntegrationTypeId(anyLong());

		assertNotNull(operationCompletionRS);
		assertEquals(
				"All global integrations with type ='EMAIL' integrations have been successfully removed.",
				operationCompletionRS.getResultMessage()
		);
	}

	@Test
	void deleteProjectIntegration() {

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		Project project = new Project();
		project.setId(projectId);
		project.setName(TEST_PROJECT_NAME);
		project.setKey(TEST_PROJECT_KEY);

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
				projectId
		);

		when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(project));

		when(integrationRepository.findByIdAndProjectId(emailIntegrationId,
				projectId
		)).thenReturn(Optional.of(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId, projectId)));

		doNothing().when(integrationRepository).deleteById(emailIntegrationId);

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegration(emailIntegrationId,
        TEST_PROJECT_KEY,
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

		Project project = new Project();
		project.setId(projectId);
		project.setName(TEST_PROJECT_NAME);
		project.setKey(TEST_PROJECT_KEY);

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, projectId);

		when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(project));

		when(integrationRepository.findAllByProjectIdOrderByCreationDateDesc(projectId)).thenReturn(Lists.newArrayList(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId,
				projectId
		)));

		when(integrationTypeRepository.findByName(anyString())).thenReturn(Optional.ofNullable(IntegrationTestUtil.getEmailIntegrationType()));

		OperationCompletionRS operationCompletionRS = deleteIntegrationHandler.deleteProjectIntegrationsByType("EMAIL",
        TEST_PROJECT_KEY,
				user
		);
		verify(integrationRepository, times(1)).deleteAllByProjectIdAndIntegrationTypeId(anyLong(), anyLong());

		assertNotNull(operationCompletionRS);
		assertEquals(
				"All integrations with type ='EMAIL' for project with name ='project Name' have been successfully deleted",
				operationCompletionRS.getResultMessage()
		);

	}

}
