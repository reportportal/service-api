/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationBuilderTest {

	@Test
	void integrationBuilderTest() {
		final Project project = new Project();
		project.setId(1L);
		project.setName("project");
		final IntegrationParams params = new IntegrationParams();
		final HashMap<String, Object> parameters = Maps.newHashMap();
		parameters.put("param1", "val1");
		parameters.put("param2", "val2");
		params.setParams(parameters);
		final IntegrationType type = new IntegrationType();
		type.setName("type");

		final String name = "name";
		final boolean enabled = true;
		final LocalDateTime creationDate = LocalDateTime.now();
		final String creator = "creator";

		final Integration integration = new IntegrationBuilder().withName(name)
				.withEnabled(enabled)
				.withCreationDate(creationDate)
				.withProject(project)
				.withCreator(creator)
				.withParams(params)
				.withType(type)
				.get();

		assertNotNull(integration);
		assertEquals(name, integration.getName());
		assertEquals(enabled, integration.isEnabled());
		assertEquals(creationDate, integration.getCreationDate());
		assertEquals(creator, integration.getCreator());
		assertEquals(project, integration.getProject());
		assertThat(integration.getType()).isEqualToComparingFieldByField(type);
		assertEquals(params.getParams(), integration.getParams().getParams());
	}

	@Test
	void updateExistIntegrationTest() {
		final Integration integration = new Integration();
		integration.setName("name");
		integration.setEnabled(false);

		final Project project = new Project();
		project.setId(1L);
		project.setName("project");
		final IntegrationParams params = new IntegrationParams();
		final HashMap<String, Object> parameters = Maps.newHashMap();
		parameters.put("param1", "val1");
		parameters.put("param2", "val2");
		params.setParams(parameters);
		final IntegrationType type = new IntegrationType();
		type.setName("type");

		final String name = "name";
		final boolean enabled = true;
		final LocalDateTime creationDate = LocalDateTime.now();
		final String creator = "creator";

		final Integration updatedIntegration = new IntegrationBuilder(integration).withName(name)
				.withEnabled(enabled)
				.withCreationDate(creationDate)
				.withProject(project)
				.withCreator(creator)
				.withParams(params)
				.withType(type)
				.get();

		assertNotNull(updatedIntegration);
		assertEquals(name, updatedIntegration.getName());
		assertEquals(enabled, updatedIntegration.isEnabled());
		assertEquals(creationDate, updatedIntegration.getCreationDate());
		assertEquals(creator, updatedIntegration.getCreator());
		assertEquals(project, updatedIntegration.getProject());
		assertThat(integration.getType()).isEqualToComparingFieldByField(type);
		assertEquals(params.getParams(), updatedIntegration.getParams().getParams());
	}
}