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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class BasicIntegrationServiceImplTest {

	private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private final PluginBox pluginBox = mock(PluginBox.class);

	private final BasicIntegrationServiceImpl basicIntegrationService = new BasicIntegrationServiceImpl(integrationRepository, pluginBox);

	@Test
	void retrieveIntegrationParams() {
		Map<String, Object> init = Maps.newHashMap();
		Map<String, Object> params = basicIntegrationService.retrieveIntegrationParams(init);
		assertEquals(params, init);
	}

	@Test
	void validateGlobalIntegrationPositive() {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integration.setType(integrationType);
		when(integrationRepository.findAllGlobalByType(integrationType)).thenReturn(Lists.newArrayList());

		//when
		boolean b = basicIntegrationService.validateIntegration(integration);

		//then
		assertTrue(b);
	}

	@Test
	void validateGlobalIntegrationNegative() {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integrationType.setName("email");
		integration.setType(integrationType);
		when(integrationRepository.findAllGlobalByType(integrationType)).thenReturn(Lists.newArrayList(new Integration()));

		//when
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> basicIntegrationService.validateIntegration(integration)
		);

		//then
		assertEquals("Impossible interact with integration. Integration with type email is already exists", exception.getMessage());
	}

	@Test
	void validateProjectIntegrationPositive() {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integration.setType(integrationType);

		Project project = new Project();
		project.setId(1L);

		when(integrationRepository.findAllByProjectIdAndType(1L, integrationType)).thenReturn(Lists.newArrayList());

		//when
		boolean b = basicIntegrationService.validateIntegration(integration, project);

		//then
		assertTrue(b);
	}

	@Test
	void validateProjectIntegrationNegative() {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integrationType.setName("email");
		integration.setType(integrationType);

		Project project = new Project();
		project.setId(1L);
		project.setName("default");

		when(integrationRepository.findAllByProjectIdAndType(1L, integrationType)).thenReturn(Lists.newArrayList(new Integration()));

		//when
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> basicIntegrationService.validateIntegration(integration, project)
		);

		//then
		assertEquals(
				"Impossible interact with integration. Integration with type email is already exists for project default",
				exception.getMessage()
		);
	}

}