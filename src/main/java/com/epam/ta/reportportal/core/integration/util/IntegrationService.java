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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface IntegrationService {

	/**
	 * Create {@link Integration} with {@link Integration#project == NULL}
	 *
	 * @param integrationTypeName {@link com.epam.ta.reportportal.entity.integration.IntegrationType#name}
	 * @param integrationParams   {@link com.epam.ta.reportportal.entity.integration.IntegrationParams#params}
	 * @return new {@link Integration}
	 */
	Integration createGlobalIntegration(String integrationTypeName, IntegrationGroupEnum integrationGroup,
			Map<String, Object> integrationParams);

	/**
	 * Create {@link Integration} for {@link com.epam.ta.reportportal.entity.project.Project} with provided ID
	 *
	 * @param integrationTypeName {@link com.epam.ta.reportportal.entity.integration.IntegrationType#name}
	 * @param projectDetails      {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param integrationParams   {@link com.epam.ta.reportportal.entity.integration.IntegrationParams#params}
	 * @return new {@link Integration}
	 */
	Integration createProjectIntegration(String integrationTypeName, IntegrationGroupEnum integrationGroup,
			ReportPortalUser.ProjectDetails projectDetails, Map<String, Object> integrationParams);

	/**
	 * Update {@link Integration} with {@link Integration#project == NULL}
	 *
	 * @param id                {@link Integration#id}
	 * @param integrationParams {@link com.epam.ta.reportportal.entity.integration.IntegrationParams#params}
	 * @return updated {@link Integration}
	 */
	Integration updateGlobalIntegration(Long id, Map<String, Object> integrationParams);

	/**
	 * Updated {@link Integration} for {@link com.epam.ta.reportportal.entity.project.Project} with provided ID
	 *
	 * @param id                {@link Integration#id}
	 * @param projectDetails    {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param integrationParams {@link com.epam.ta.reportportal.entity.integration.IntegrationParams#params}
	 * @return updated {@link Integration}
	 */
	Integration updateProjectIntegration(Long id, ReportPortalUser.ProjectDetails projectDetails, Map<String, Object> integrationParams);
}
