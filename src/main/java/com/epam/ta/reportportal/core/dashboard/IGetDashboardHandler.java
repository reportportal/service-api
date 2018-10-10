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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.springframework.data.domain.Pageable;

/**
 * Get dashboard handler.
 *
 * @author Aliaksei_Makayed
 */
public interface IGetDashboardHandler {

	/**
	 * Get dashboard by id
	 *
	 * @param dashboardId    Dashboard id
	 * @param projectDetails Project details
	 * @param user           User
	 * @return {@link DashboardResource}
	 */
	DashboardResource getDashboard(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get all dashboards.
	 *
	 * @param projectDetails Project details
	 * @param user           User
	 * @return {@link Iterable}
	 * @throws ReportPortalException
	 */
	Iterable<DashboardResource> getAllDashboards(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get dashboards names shared for current project.
	 * Result map:<br>
	 * <li>key - dashboard id,
	 * <li>value - dashboard name
	 *
	 * @param projectName
	 * @return {@link Iterable}
	 * @throws ReportPortalException
	 */
	Iterable<SharedEntity> getSharedDashboardsNames(String ownerName, String projectName, Pageable pageable);
}