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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.BtsConnectionTestRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateBugTrackingSystemRQ;

/**
 * Basic interface for {@link com.epam.ta.reportportal.core.bts.handler.impl.UpdateBugTrackingSystemHandlerImpl}
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface UpdateBugTrackingSystemHandler {

	/**
	 * Update method for {@link com.epam.ta.reportportal.entity.integration.Integration} entity
	 * with {@link com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum#BTS}
	 *
	 * @param updateRequest Request Data
	 * @param integrationId BugTrackingSystem id
	 * @return Operation result
	 */
	OperationCompletionRS updateGlobalBugTrackingSystem(UpdateBugTrackingSystemRQ updateRequest, Long integrationId);

	/**
	 * Update method for {@link com.epam.ta.reportportal.entity.integration.Integration} entity
	 * with {@link com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum#BTS}
	 *
	 * @param updateRequest  Request Data
	 * @param integrationId  BugTrackingSystem id
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param user           {@link ReportPortalUser}
	 * @return Operation result
	 */
	OperationCompletionRS updateProjectBugTrackingSystem(UpdateBugTrackingSystemRQ updateRequest, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Validate connection of provided BugTrackingSystem configuration
	 *
	 * @param connectionTestRQ {@link BtsConnectionTestRQ}
	 * @param integrationId    BugTrackingSystem id
	 * @param projectDetails   {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return Operation result
	 */
	OperationCompletionRS integrationConnect(BtsConnectionTestRQ connectionTestRQ, Long integrationId,
			ReportPortalUser.ProjectDetails projectDetails);

}