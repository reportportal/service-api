/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

/**
 * Report Portal WS Interface. Dashboard controller.
 *
 * @author Aliaksei_Makayed
 */
public interface IDashboardController {

	/**
	 * Create new dashboard object
	 *
	 * @param projectName Project Name
	 * @param createRQ    Request Data
	 * @param principal   Login
	 * @return EntryCreatedRS
	 * @throws ReportPortalException
	 */
	EntryCreatedRS createDashboard(String projectName, CreateDashboardRQ createRQ, Principal principal);

	/**
	 * Get all dashboards for current user
	 *
	 * @param projectName Project Name
	 * @param principal   Login
	 * @return Iterable<DashboardResource>
	 * @throws ReportPortalException
	 */
	Iterable<DashboardResource> getAllDashboards(String projectName, Principal principal);

	/**
	 * Get specified dashboard by its ID for specified project
	 *
	 * @param projectName Project Name
	 * @param dashboardId Dashboard ID
	 * @param principal   Login
	 * @return Found Dashboard
	 */
	DashboardResource getDashboard(String projectName, String dashboardId, Principal principal);

	/**
	 * Update dashboard with specified id.
	 *
	 * @param dashboardId Dashboard ID
	 * @param projectName Project Name
	 * @param principal   Login
	 * @param updateRQ    Request Data
	 * @return OperationCompletionRS
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateDashboard(String projectName, String dashboardId, UpdateDashboardRQ updateRQ, Principal principal,
			UserRole userRole);

	/**
	 * Delete dashboard with specified id.
	 *
	 * @param dashboardId Dashboard ID
	 * @param projectName Project Name
	 * @param principal   Login
	 * @return OperationCompletionRS
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteDashboard(String projectName, String dashboardId, UserRole userRole, Principal principal);

	/**
	 * Get all dashboards for current user
	 *
	 * @param projectName Project Name
	 * @param principal   Login
	 * @param pageable    Page
	 * @return Iterable<SharedEntity>
	 * @throws ReportPortalException
	 */
	Iterable<SharedEntity> getSharedDashboardsNames(String projectName, Principal principal, Pageable pageable);
}