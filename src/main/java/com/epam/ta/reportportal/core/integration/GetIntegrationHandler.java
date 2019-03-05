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

package com.epam.ta.reportportal.core.integration;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;

import java.util.Optional;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public interface GetIntegrationHandler {

	/**
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return {@link IntegrationResource}
	 */
	IntegrationResource getProjectIntegrationById(Long integrationId, ReportPortalUser.ProjectDetails projectDetails);

	IntegrationResource getGlobalIntegrationById(Long integrationId);

	Optional<Integration> getEnabledByProjectIdOrGlobalAndIntegrationGroup(Long projectId, IntegrationGroupEnum integrationGroup);

	Integration getEnabledBtsIntegration(ReportPortalUser.ProjectDetails projectDetails, String url, String btsProject);

	Integration getEnabledBtsIntegration(ReportPortalUser.ProjectDetails projectDetails, Long integrationId);
}
