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

import java.util.List;
import java.util.function.Function;

import static com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler.pageConverter;
import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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
		return TO_RESOURCE.apply(userFilter);
	}

	@Override
	public List<UserFilterResource> getOwnFilters(String userName, Filter filter, String projectName) {
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, false);
		return filters.stream().map(TO_RESOURCE).collect(toList());
	}

	@Override
	public List<UserFilterResource> getSharedFilters(String userName, Filter filter, String projectName) {
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, true);
		return filters.stream().map(TO_RESOURCE).collect(toList());
	}

	@Override
	public Iterable<UserFilterResource> getFilters(String userName, Pageable pageable, Filter filter, String projectName) {
		Page<UserFilter> filters = filterRepository.findAllByFilter(filter, pageable, projectName, userName);
		return pageConverter(TO_RESOURCE).apply(filters);
	}

	@Override
	public Iterable<SharedEntity> getFiltersNames(String userName, String projectName, boolean isShared) {
		// filter names should be selected per user, projectName and only for
		// userFilter
		// with field isLink = false
		List<UserFilter> filters = filterRepository.findFilters(userName, projectName, Shareable.NAME_OWNER_SORT, isShared);
		return filters.stream().map(TO_SHARED_ENTITY).collect(toList());
	}

	@Override
	public List<UserFilterResource> getFilters(String projectName, String[] ids, String userName) {
		Iterable<UserFilter> filters = filterRepository.findAvailableFilters(projectName, ids, userName);
		return resourceAssembler.toResources(filters);
	}

	/**
	 * Convert {@code UserFilter to SharedEntity}.
	 *
	 * @return SharedEntity
	 */
	private final Function<UserFilter, SharedEntity> TO_SHARED_ENTITY = filter -> {
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setId(filter.getId());
		sharedEntity.setName(filter.getName());
		ofNullable(filter.getAcl()).ifPresent(acl -> sharedEntity.setOwner(acl.getOwnerUserId()));
		sharedEntity.setDescription(filter.getDescription());
		return sharedEntity;
	};

}
