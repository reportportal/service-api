/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.shareable.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_ADMINISTRATE_OBJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetShareableDashboardHandlerImpl implements GetShareableEntityHandler<Dashboard> {

	private final DashboardRepository dashboardRepository;

	@Autowired
	public GetShareableDashboardHandlerImpl(DashboardRepository dashboardRepository) {
		this.dashboardRepository = dashboardRepository;
	}

	@Override
	@PostAuthorize(CAN_READ_OBJECT)
	public Dashboard getPermitted(Long id, ReportPortalUser.ProjectDetails projectDetails) {
		return dashboardRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT, id, projectDetails.getProjectName()));
	}

	@Override
	@PostAuthorize(CAN_ADMINISTRATE_OBJECT)
	public Dashboard getAdministrated(Long id, ReportPortalUser.ProjectDetails projectDetails) {
		return dashboardRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND_IN_PROJECT, id, projectDetails.getProjectName()));
	}
}
