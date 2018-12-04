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

package com.epam.ta.reportportal.core.dashboard;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface DeleteDashboardHandler {

	/**
	 * Delete {@link com.epam.ta.reportportal.entity.dashboard.Dashboard} instance with specified id
	 *
	 * @param dashboardId    Dashboard id
	 * @param projectDetails Project details
	 * @param user           User
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS deleteDashboard(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

}
