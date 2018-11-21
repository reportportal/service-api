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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;

import java.util.List;

/**
 * {@link FinishExecutionRQ} request handler
 *
 * @author Andrei_Kliashchonak
 */

public interface FinishLaunchHandler {

	/**
	 * Updates {@link Launch} instance
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return FinishLaunchRS
	 */
	FinishLaunchRS finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Stop Launch instance by user
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Bulk stop launches operation.
	 *
	 * @param bulkRQ         Bulk request
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return OperationCompletionsRS
	 */
	List<OperationCompletionRS> stopLaunch(BulkRQ<FinishExecutionRQ> bulkRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);
}