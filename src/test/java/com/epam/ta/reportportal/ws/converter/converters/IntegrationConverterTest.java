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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.IntegrationAuthFlowEnum;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationConverterTest {

	@Test
	void toResource() {
		final Integration integration = getIntegration();
		final IntegrationResource resource = IntegrationConverter.TO_INTEGRATION_RESOURCE.apply(integration);

		assertEquals(resource.getCreationDate(), Date.from(integration.getCreationDate().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getEnabled(), integration.isEnabled());
		assertEquals(resource.getId(), integration.getId());
		assertEquals(resource.getProjectId(), integration.getProject().getId());

		assertThat(resource.getIntegrationParams()).containsOnlyKeys("param1", "param2", "nullParam", null);
		assertThat(resource.getIntegrationParams()).doesNotContainKey("accessToken");
		assertThat(resource.getIntegrationParams()).containsValues("qwerty", "asdfgh", "value", null);

		final IntegrationTypeResource integrationTypeResource = resource.getIntegrationType();

		assertEquals(integrationTypeResource.getAuthFlow().name(), integration.getType().getAuthFlow().name());
		assertEquals(integrationTypeResource.getId(), integration.getType().getId());
		assertEquals(integrationTypeResource.getName(), integration.getType().getName());
		assertEquals(integrationTypeResource.getGroupType(), integration.getType().getIntegrationGroup().name());
	}

	@Test
	void toActivityResource() {
		final Integration integration = getIntegration();
		final IntegrationActivityResource resource = IntegrationConverter.TO_ACTIVITY_RESOURCE.apply(integration);

		assertEquals(resource.getId(), integration.getId());
		assertEquals(resource.getProjectId(), integration.getProject().getId());
		assertEquals(resource.getProjectName(), integration.getProject().getName());
		assertEquals(resource.getTypeName(), integration.getType().getName());
	}

	private static Integration getIntegration() {
		Integration integration = new Integration();
		final IntegrationType type = new IntegrationType();
		type.setCreationDate(LocalDateTime.now());
		type.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
		type.setName("typeName");
		type.setAuthFlow(IntegrationAuthFlowEnum.BASIC);
		type.setEnabled(true);
		type.setId(1L);
		type.setIntegrations(Sets.newHashSet(integration));
		integration.setType(type);
		final IntegrationParams params = new IntegrationParams();
		final HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("param1", "qwerty");
		paramMap.put("param2", "asdfgh");
		paramMap.put("nullParam", null);
		paramMap.put(null, "value");
		paramMap.put("accessToken", "asdfgh");
		params.setParams(paramMap);
		integration.setParams(params);
		integration.setEnabled(true);
		final Project project = new Project();
		project.setId(2L);
		project.setName("projectName");
		integration.setProject(project);
		integration.setId(3L);
		integration.setCreationDate(LocalDateTime.now());
		return integration;
	}
}