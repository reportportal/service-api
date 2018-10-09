/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.impl.UpdateExternalSystemHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;

/**
 * Basic interface for {@link UpdateExternalSystemHandler}
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface IUpdateExternalSystemHandler {

	/**
	 * Update method for {@link com.epam.ta.reportportal.entity.bts.BugTrackingSystem} entity
	 *
	 * @param request     Request Data
	 * @param projectName Project Name
	 * @param id          System ID
	 * @param user        Report portal user
	 * @return Operation result
	 */
	OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, Long id, ReportPortalUser user);

	/**
	 * Validate connection of provided ExternalSystem configuration
	 *
	 * @param projectName Project
	 * @param systemId    External system id
	 * @param updateRQ    Request Data
	 * @param user        Report portal user
	 * @return Operation result
	 */
	OperationCompletionRS externalSystemConnect(UpdateExternalSystemRQ updateRQ, String projectName, Long systemId, ReportPortalUser user);

}