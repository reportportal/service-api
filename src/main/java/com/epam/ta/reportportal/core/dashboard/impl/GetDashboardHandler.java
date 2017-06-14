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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.dashboard.IGetDashboardHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.util.MoreCollectors;
import com.epam.ta.reportportal.ws.converter.DashboardResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IGetDashboardHandler}
 *
 * @author Aliaksei_Makayed
 *
 */
@Service
public class GetDashboardHandler implements IGetDashboardHandler {

	private final DashboardRepository dashboardRepository;

	private final DashboardResourceAssembler resourceAssembler;

	private final Sort creationDateSort;

	@Autowired
	public GetDashboardHandler(DashboardRepository dashboardRepository, DashboardResourceAssembler resourceAssembler) {
		creationDateSort = new Sort(new Order(Direction.ASC, Dashboard.CREATION_DATE));
		this.dashboardRepository = dashboardRepository;
		this.resourceAssembler = resourceAssembler;
	}

	@Override
	public DashboardResource getDashboard(String dashboardId, String userName, String projectName) {
		Dashboard dashboard = dashboardRepository.findOne(dashboardId);
		BusinessRule.expect(dashboard, Predicates.notNull()).verify(ErrorType.DASHBOARD_NOT_FOUND, dashboardId);
		AclUtils.isPossibleToRead(dashboard.getAcl(), userName, projectName);
		BusinessRule.expect(dashboard.getProjectName(), Predicates.equalTo(projectName)).verify(ErrorType.ACCESS_DENIED);
		return resourceAssembler.toResource(dashboard);
	}

	@Override
	public Iterable<DashboardResource> getAllDashboards(String userName, String projectName) {
		// Get list of owned dashboards
		List<Dashboard> dashboards = dashboardRepository.findAll(userName, creationDateSort, projectName);
		return resourceAssembler.toResources(dashboards);
	}

	@Override
	public Map<String, SharedEntity> getSharedDashboardsNames(String ownerName, String projectName) {
		List<Dashboard> dashboards = dashboardRepository.findSharedEntities(ownerName, projectName,
				Lists.newArrayList(Shareable.ID, Dashboard.NAME, Dashboard.OWNER, "description"), Shareable.NAME_OWNER_SORT);
		return toMap(dashboards);
	}

	/**
	 * Transform {@link List} of {@link Dashboard}s to {@link java.util.Map} where:<br>
	 * <li>key - dashboard id,
	 * <li>value - shared entity
	 *
	 * @param dashboards Dashboards list
	 * @return Transformed map
	 */
	private Map<String, SharedEntity> toMap(List<Dashboard> dashboards) {
		return dashboards.stream()
				.collect(MoreCollectors.toLinkedMap(Dashboard::getId, dashboard -> {
					SharedEntity sharedEntity = new SharedEntity();
					sharedEntity.setName(dashboard.getName());
					sharedEntity.setDescription(dashboard.getDescription());
					if (null != dashboard.getAcl()) {
						sharedEntity.setOwner(dashboard.getAcl().getOwnerUserId());
					}
					return sharedEntity;
				}));
	}

}
