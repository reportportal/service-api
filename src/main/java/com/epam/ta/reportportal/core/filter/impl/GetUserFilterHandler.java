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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.commons.MoreCollectors;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.filter.IGetUserFilterHandler;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.converter.UserFilterResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IGetUserFilterHandler}
 *
 * @author Aliaksei_Makayed
 */

@Service
public class GetUserFilterHandler implements IGetUserFilterHandler {

	@Autowired
	private UserFilterRepository filterRepository;

	@Autowired
	private UserFilterResourceAssembler resourceAssembler;

	@Override
	public UserFilterResource getFilter(String userName, String filterId, String projectName) {
		UserFilter userFilter = filterRepository.findOne(filterId);
		BusinessRule.expect(userFilter, Predicates.notNull()).verify(ErrorType.USER_FILTER_NOT_FOUND, filterId, userName);
		AclUtils.isPossibleToRead(userFilter.getAcl(), userName, projectName);
		BusinessRule.expect(userFilter.getProjectName(), Predicates.equalTo(projectName)).verify(ErrorType.ACCESS_DENIED);
		return resourceAssembler.toResource(userFilter);
	}

	@Override
	public List<UserFilterResource> getOwnFilters(String userName, Filter filter, String projectName) {
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, false);
		return resourceAssembler.toResources(filters);
	}

	@Override
	public List<UserFilterResource> getSharedFilters(String userName, Filter filter, String projectName) {
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, true);
		return resourceAssembler.toResources(filters);
	}

	@Override
	public Iterable<UserFilterResource> getFilters(String userName, Pageable pageable, Filter filter, String projectName) {
		Page<UserFilter> filters = filterRepository.findAllByFilter(filter, pageable, projectName, userName);
		return resourceAssembler.toPagedResources(filters);
	}

	@Override
	public Map<String, SharedEntity> getFiltersNames(String userName, String projectName, boolean isShared) {
		// filter names should be selected per user, projectName and only for
		// userFilter
		// with field isLink = false
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, isShared);
		return getMapOfNames(filters);
	}

	@Override
	public List<UserFilterResource> getFilters(String projectName, String[] ids, String userName) {
		Iterable<UserFilter> filters = filterRepository.findAvailableFilters(projectName, ids, userName);
		return resourceAssembler.toResources(filters);
	}

	/**
	 * Convert {@code List<UserFilter> to Map<String, SharedEntity>}. Resulted
	 * map:<br>
	 * key - userFilter's id value - shared entity
	 *
	 * @param filters
	 * @return Map<String, SharedEntity>
	 */
	private Map<String, SharedEntity> getMapOfNames(List<UserFilter> filters) {
		Map<String, SharedEntity> result = Collections.emptyMap();
		if (filters != null) {
			result = filters.stream().collect(MoreCollectors.toLinkedMap(UserFilter::getId, filter -> {
				SharedEntity entity = new SharedEntity();
				entity.setName(filter.getName());
				if (null != filter.getAcl()) {
					entity.setOwner(filter.getAcl().getOwnerUserId());
				}
				return entity;
			}));
		}

		return result;
	}
}
