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

package com.epam.ta.reportportal.core.dashboard;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;

/**
 * @author Pavel Bortnik
 */
public interface IUpdateDashboardHandler {

	/**
	 * Update dashboard with specified id
	 *
	 * @param projectDetails Project details
	 * @param rq             Update details
	 * @param dashboardId    Dashboard id to be updated
	 * @param user           User
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS updateDashboard(ReportPortalUser.ProjectDetails projectDetails, UpdateDashboardRQ rq, Long dashboardId,
			ReportPortalUser user);

	/**
	 * Add a new widget to the specified dashboard
	 *
	 * @param dashboardId    Dashboard id
	 * @param projectDetails Project details
	 * @param rq             Widget details
	 * @param user           User
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS addWidget(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, AddWidgetRq rq,
			ReportPortalUser user);

	/**
	 * Removes a specified widget from the specified dashboard
	 *
	 * @param widgetId       Widget id
	 * @param dashboardId    Dashboard id
	 * @param projectDetails Project details
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS removeWidget(Long widgetId, Long dashboardId, ReportPortalUser.ProjectDetails projectDetails);

}
