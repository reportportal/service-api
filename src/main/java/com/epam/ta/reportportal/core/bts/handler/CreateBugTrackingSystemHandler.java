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
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateBugTrackingSystemRQ;

/**
 * Basic interface for {@link} realization
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface CreateBugTrackingSystemHandler {

	/**
	 * Create {@link com.epam.ta.reportportal.entity.integration.Integration} entry method
	 *
	 * @param createRequest       Request Data
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @return Response Data
	 */
	EntryCreatedRS createBugTrackingSystem(CreateBugTrackingSystemRQ createRequest, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}