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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.dashboard.IGetDashboardHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.FavoriteResourceRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.DashboardResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.favorites.FavoriteResourceTypes;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Default implementation of {@link IGetDashboardHandler}
 *
 * @author Aliaksei_Makayed
 *
 */
@Service
public class GetDashboardHandler implements IGetDashboardHandler {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private DashboardResourceAssembler resourceAssembler;

	@Autowired
	private FavoriteResourceRepository favoriteResourceRepository;

	private final Sort creationDateSort;

	public GetDashboardHandler() {
		creationDateSort = new Sort(new Order(Direction.ASC, Dashboard.CREATION_DATE));
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
		// Get list of registered favorites recources for specified user
		List<FavoriteResource> favoriteResources = favoriteResourceRepository
				.findByFilter(getFavoriteDashboardsFilter(userName, projectName));
		List<String> favoriteDashboardsIds = favoriteResources.stream().map(FavoriteResource::getResourceId).collect(Collectors.toList());

		// Get list of REAL existing dashboards by Ids
		Iterable<Dashboard> favoriteDashboars = dashboardRepository.findAll(favoriteDashboardsIds);

		// Get list of owned dashboards
		List<Dashboard> dashboards = dashboardRepository.findAll(userName, creationDateSort, projectName);
		dashboards.addAll(Lists.newArrayList(favoriteDashboars));
		addRemovedDashboards(favoriteDashboardsIds, dashboards);
		return resourceAssembler.toResources(dashboards);
	}

	@Override
	public Map<String, SharedEntity> getSharedDashboardsNames(String ownerName, String projectName) {
		List<Dashboard> dashboards = dashboardRepository.findSharedEntities(ownerName, projectName,
				Lists.newArrayList(Shareable.ID, Dashboard.NAME, Dashboard.OWNER), Shareable.NAME_OWNER_SORT);
		return toMap(dashboards);
	}

	/**
	 * Add empty dashboard objects to dashboards list - if dashboard have
	 * already removed but still present in favorite resources for user it
	 * should be added to result list.
	 */
	private void addRemovedDashboards(List<String> ids, List<Dashboard> dashboards) {
		if (Preconditions.NOT_EMPTY_COLLECTION.test(ids) && Preconditions.NOT_EMPTY_COLLECTION.test(dashboards)) {
			Set<String> dashHashSet = dashboards.stream().map(Dashboard::getId).collect(Collectors.toSet());
			List<Dashboard> additionalDashboards = ids.stream().filter(id -> !dashHashSet.contains(id)).map(id -> {
				final Dashboard dashboard = new Dashboard();
				dashboard.setId(id);
				return dashboard;
			}).collect(Collectors.toList());
			dashboards.addAll(additionalDashboards);
		}
	}

	/**
	 * Transform {@link List} of {@link Dashboard}s to {@value Map} where:<br>
	 * <li>key - dashboard id,
	 * <li>value - shared entity
	 *
	 * @param dashboards
	 * @return
	 */
	private Map<String, SharedEntity> toMap(List<Dashboard> dashboards) {
		Map<String, SharedEntity> result = new LinkedHashMap<>();
		for (Dashboard dashboard : dashboards) {
			SharedEntity sharedEntity = new SharedEntity();
			sharedEntity.setName(dashboard.getName());
			if (null != dashboard.getAcl()) {
				sharedEntity.setOwner(dashboard.getAcl().getOwnerUserId());
			}
			result.put(dashboard.getId(), sharedEntity);
		}
		return result;
	}

	private Filter getFavoriteDashboardsFilter(String userName, String projectName) {
		FilterCondition resourceTypeCondition = new FilterCondition(Condition.EQUALS, false, FavoriteResourceTypes.DASHBOARD.name(),
				FavoriteResource.TYPE_CRITERIA);
		FilterCondition userNameCondition = new FilterCondition(Condition.EQUALS, false, userName, FavoriteResource.USERNAME_CRITERIA);
		FilterCondition projectCondition = new FilterCondition(Condition.EQUALS, false, projectName, FavoriteResource.PROJECT_CRITERIA);
		return new Filter(FavoriteResource.class, Sets.newHashSet(resourceTypeCondition, userNameCondition, projectCondition));
	}
}