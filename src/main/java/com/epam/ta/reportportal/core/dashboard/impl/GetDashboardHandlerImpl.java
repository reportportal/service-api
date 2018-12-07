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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.DashboardConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_ADMINISTRATE_OBJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetDashboardHandlerImpl implements GetDashboardHandler {

	private DashboardRepository dashboardRepository;

	@Autowired
	public void setDashboardRepository(DashboardRepository dashboardRepository) {
		this.dashboardRepository = dashboardRepository;
	}

	@Override
	@PostAuthorize(CAN_READ_OBJECT)
	public Dashboard getPermitted(Long dashboardId) {
		return dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND, dashboardId));
	}

	@Override
	@PostAuthorize(CAN_ADMINISTRATE_OBJECT)
	public Dashboard getAdministrated(Long dashboardId) {
		return dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND, dashboardId));
	}

	@Override
	public Iterable<DashboardResource> getPermitted(ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Dashboard> permitted = dashboardRepository.getPermitted(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(DashboardConverter.TO_RESOURCE).apply(permitted);
	}

	@Override
	public Iterable<SharedEntity> getSharedDashboardsNames(ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Dashboard> shared = dashboardRepository.getShared(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(DashboardConverter.TO_SHARED_ENTITY).apply(shared);
	}
}
