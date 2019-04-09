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

package com.epam.ta.reportportal.core.integration;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreateIntegrationHandler {

	/**
	 * Create {@link Integration} with {@link Integration#project == NULL}
	 *
	 * @param updateRequest
	 * @return new {@link Integration}
	 */
	OperationCompletionRS createGlobalIntegration(UpdateIntegrationRQ updateRequest);

	/**
	 * Create {@link Integration} for {@link com.epam.ta.reportportal.entity.project.Project} with provided ID
	 *
	 * @param projectDetails
	 * @param updateRequest
	 * @param user
	 * @return new {@link Integration}
	 */
	OperationCompletionRS createProjectIntegration(ReportPortalUser.ProjectDetails projectDetails, UpdateIntegrationRQ updateRequest,
			ReportPortalUser user);

	/**
	 * Update {@link Integration} with {@link Integration#project == NULL}
	 *
	 * @param id            {@link Integration#id}
	 * @param updateRequest
	 * @return updated {@link Integration}
	 */
	OperationCompletionRS updateGlobalIntegration(Long id, UpdateIntegrationRQ updateRequest);

	/**
	 * Updated {@link Integration} for {@link com.epam.ta.reportportal.entity.project.Project} with provided ID
	 *
	 * @param id             {@link Integration#id}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param updateRequest
	 * @param user
	 * @return updated {@link Integration}
	 */
	OperationCompletionRS updateProjectIntegration(Long id, ReportPortalUser.ProjectDetails projectDetails, UpdateIntegrationRQ updateRequest, ReportPortalUser user);
}
