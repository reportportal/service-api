/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.impl.util;

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.entity.project.Project;
import com.google.common.collect.Maps;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class IntegrationTestUtil {

	private IntegrationTestUtil() {

		//static only
	}

	public static Integration getGlobalEmailIntegration(long emailIntegrationId) {

		Integration integration = new Integration();

		integration.setCreator("superadmin");
		integration.setCreationDate(LocalDateTime.now());
		integration.setType(getEmailIntegrationType());
		integration.setParams(new IntegrationParams(getParams()));
		integration.setId(emailIntegrationId);

		return integration;
	}

	public static Integration getProjectEmailIntegration(long emailIntegrationId, long projectId) {

		Integration integration = getGlobalEmailIntegration(emailIntegrationId);

		integration.setProject(getProjectWithId(projectId).get());

		return integration;
	}

	public static Integration getGlobalJiraIntegration(long id, Map<String, Object> params) {

		Integration integration = new Integration();

		integration.setCreator("superadmin");
		integration.setCreationDate(LocalDateTime.now());
		integration.setType(getJiraIntegrationType());
		integration.setParams(new IntegrationParams(params));
		integration.setId(id);

		return integration;
	}

	public static Integration getProjectJiraIntegration(long id, Map<String, Object> params, long projectId) {

		Integration integration = getGlobalJiraIntegration(id, params);

		integration.setProject(getProjectWithId(projectId).get());

		return integration;
	}

	public static Map<String, Object> getParams() {

		Map<String, Object> map = Maps.newHashMap();
		map.put("first", "first");
		map.put("second", "second");
		return map;
	}

	public static Optional<Project> getProjectWithId(long projectId) {
		Project project = new Project();

		project.setId(projectId);

		return Optional.of(project);
	}

	public static IntegrationType getJiraIntegrationType() {

		IntegrationType integrationType = new IntegrationType();

		integrationType.setName("jira");
		integrationType.setCreationDate(LocalDateTime.now());
		integrationType.setId(1L);
		integrationType.setIntegrationGroup(IntegrationGroupEnum.BTS);
		IntegrationTypeDetails details = new IntegrationTypeDetails();
		details.setDetails(Maps.newHashMap());
		integrationType.setDetails(details);

		return integrationType;
	}

	public static IntegrationType getEmailIntegrationType() {

		IntegrationType integrationType = new IntegrationType();

		integrationType.setName("EMAIL");
		integrationType.setCreationDate(LocalDateTime.now());
		integrationType.setId(1L);
		integrationType.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);

		return integrationType;
	}
}
