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
import com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.springframework.data.domain.Pageable;

/**
 * Get dashboard handler.
 *
 * @author Aliaksei_Makayed
 */
public interface GetDashboardHandler {

	/**
	 * Get dashboard on which use have {@link com.epam.ta.reportportal.auth.permissions.AclReadPermission} by id
	 *
	 * @param dashboardId    Dashboard id
	 * @return dashboard
	 */
	Dashboard getPermitted(Long dashboardId);

	/**
	 * Get dashboard on which use have {@link com.epam.ta.reportportal.auth.permissions.AclFullPermission} by id
	 *
	 * @param dashboardId    Dashboard id
	 * @return dashboard
	 */
	Dashboard getAdministrated(Long dashboardId);

	/**
	 * Get permitted projects for concrete user for concrete project
	 *
	 * @param projectDetails Project details
	 * @param user           User
	 * @return Page of permitted dashboard resources
	 */
	Iterable<DashboardResource> getPermitted(ProjectDetails projectDetails, Pageable pageable, Filter filter, ReportPortalUser user);

	/**
	 * Get shared dashboards entities for current project.
	 *
	 * @param projectDetails Project
	 * @param pageable       Pageable
	 * @param filter         Filter
	 * @param user           User
	 * @return {@link Iterable}
	 */
	Iterable<SharedEntity> getSharedDashboardsNames(ProjectDetails projectDetails, Pageable pageable, Filter filter, ReportPortalUser user);
}