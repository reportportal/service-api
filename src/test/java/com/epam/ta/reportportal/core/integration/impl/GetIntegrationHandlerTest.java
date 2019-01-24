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
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GetIntegrationHandlerTest {

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);

	private final GetIntegrationHandler getIntegrationHandler = new GetIntegrationHandlerImpl(integrationRepository);

	@Test
	public void getProjectIntegrationById() {

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

		IntegrationResource integrationResource = getIntegrationHandler.getProjectIntegrationById(emailIntegrationId,
				ProjectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME)
		);

		Assert.assertNotNull(integrationResource);
		Assert.assertEquals(emailIntegrationId, (long) integrationResource.getId());
		Assert.assertEquals(false, integrationResource.getEnabled());
		Assert.assertEquals(projectId, (long) integrationResource.getProjectId());
		Assert.assertNotNull(integrationResource.getIntegrationParams());
		Assert.assertNotNull(integrationResource.getIntegrationType());
	}

	@Test
	public void getGlobalIntegrationById() {

		final long emailIntegrationId = 1L;
		when(integrationRepository.findGlobalById(emailIntegrationId)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(
				emailIntegrationId)));

		IntegrationResource integrationResource = getIntegrationHandler.getGlobalIntegrationById(emailIntegrationId);

		Assert.assertNotNull(integrationResource);
		Assert.assertEquals(emailIntegrationId, (long) integrationResource.getId());
		Assert.assertEquals(false, integrationResource.getEnabled());
		Assert.assertNull(integrationResource.getProjectId());
		Assert.assertNotNull(integrationResource.getIntegrationParams());
		Assert.assertNotNull(integrationResource.getIntegrationType());
	}
}