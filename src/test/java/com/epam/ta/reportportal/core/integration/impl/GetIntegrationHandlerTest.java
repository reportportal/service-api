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

import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.GetBugTrackingSystemHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GetIntegrationHandlerTest {

	private final Map<String, IntegrationService> integrationServiceMapming = mock(Map.class);
	private final IntegrationService basicIntegrationService = mock(IntegrationService.class);
	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GetBugTrackingSystemHandler getBugTrackingSystemHandler = mock(GetBugTrackingSystemHandler.class);

	private final GetIntegrationHandler getIntegrationHandler = new GetIntegrationHandlerImpl(integrationServiceMapming,
			basicIntegrationService,
			integrationRepository,
			integrationTypeRepository,
			projectRepository,
			getBugTrackingSystemHandler
	);

	@Test
	void getProjectIntegrationById() {

		final long emailIntegrationId = 1L;
		final long projectId = 1L;

		Project project = new Project();
		project.setId(projectId);
		project.setName(TEST_PROJECT_NAME);

		final ReportPortalUser user = ReportPortalUserUtil.getRpUser("admin",
				UserRole.ADMINISTRATOR,
				ProjectRole.PROJECT_MANAGER,
				projectId
		);

		when(projectRepository.findByName(TEST_PROJECT_NAME)).thenReturn(Optional.of(project));

		when(integrationRepository.findByIdAndProjectId(emailIntegrationId,
				projectId
		)).thenReturn(Optional.of(IntegrationTestUtil.getProjectEmailIntegration(emailIntegrationId, projectId)));

		IntegrationResource integrationResource = getIntegrationHandler.getProjectIntegrationById(emailIntegrationId, TEST_PROJECT_NAME);

		assertNotNull(integrationResource);
		assertEquals(emailIntegrationId, (long) integrationResource.getId());
		assertEquals(false, integrationResource.getEnabled());
		assertEquals(projectId, (long) integrationResource.getProjectId());
		assertNotNull(integrationResource.getIntegrationParams());
		assertNotNull(integrationResource.getIntegrationType());
	}

	@Test
	void getGlobalIntegrationById() {

		final long emailIntegrationId = 1L;
		when(integrationRepository.findGlobalById(emailIntegrationId)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(
				emailIntegrationId)));

		IntegrationResource integrationResource = getIntegrationHandler.getGlobalIntegrationById(emailIntegrationId);

		assertNotNull(integrationResource);
		assertEquals(emailIntegrationId, (long) integrationResource.getId());
		assertEquals(false, integrationResource.getEnabled());
		assertNull(integrationResource.getProjectId());
		assertNotNull(integrationResource.getIntegrationParams());
		assertNotNull(integrationResource.getIntegrationType());
	}
}