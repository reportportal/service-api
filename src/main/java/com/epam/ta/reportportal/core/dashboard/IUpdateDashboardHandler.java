/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
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
