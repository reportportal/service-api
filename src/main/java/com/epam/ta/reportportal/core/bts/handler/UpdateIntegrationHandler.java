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

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateIntegrationRQ;

/**
 * Basic interface for {@link com.epam.ta.reportportal.core.bts.handler.impl.UpdateIntegrationHandlerImpl}
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface UpdateIntegrationHandler {

	/**
	 * Update method for {@link com.epam.ta.reportportal.entity.integration.Integration} entity
	 *
	 * @param request        Request Data
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param user {@link com.epam.ta.reportportal.auth.ReportPortalUser}
	 * @return Operation result
	 */
	OperationCompletionRS updateIntegration(UpdateIntegrationRQ request, Long integrationId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Validate connection of provided Integration configuration
	 *
	 * @param updateRQ       Request Data
	 * @param integrationId  Integration id
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @return Operation result
	 */
	OperationCompletionRS integrationConnect(UpdateIntegrationRQ updateRQ, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails);

}