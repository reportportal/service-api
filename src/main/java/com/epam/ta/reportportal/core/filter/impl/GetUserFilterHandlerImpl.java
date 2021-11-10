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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT_FILTER;

/**
 * @author Pavel Bortnik
 */
@Service
@Transactional(readOnly = true)
public class GetUserFilterHandlerImpl implements GetUserFilterHandler {

	private UserFilterRepository filterRepository;
	private final ProjectExtractor projectExtractor;

	@Autowired
	public GetUserFilterHandlerImpl(ProjectExtractor projectExtractor) {
		this.projectExtractor = projectExtractor;
	}

	@Autowired
	public void setFilterRepository(UserFilterRepository filterRepository) {
		this.filterRepository = filterRepository;
	}

	@Override
	public Iterable<UserFilterResource> getPermitted(String projectName, Pageable pageable, Filter filter, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user, projectName);
		Page<UserFilter> permitted = filterRepository.getPermitted(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE).apply(permitted);
	}

	@Override
	public Iterable<UserFilterResource> getOwn(String projectName, Pageable pageable, Filter filter, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user, projectName);
		Page<UserFilter> filters = filterRepository.getOwn(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE).apply(filters);
	}

	@Override
	public Iterable<UserFilterResource> getShared(String projectName, Pageable pageable, Filter filter, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user, projectName);
		Page<UserFilter> filters = filterRepository.getShared(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE).apply(filters);
	}

	@Override
	public Iterable<SharedEntity> getFiltersNames(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user, boolean isShared) {
		Page<UserFilter> filters = isShared ?
				filterRepository.getShared(ProjectFilter.of(filter, projectDetails.getProjectId()), pageable, user.getUsername()) :
				filterRepository.getOwn(ProjectFilter.of(filter, projectDetails.getProjectId()), pageable, user.getUsername());
		return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_SHARED_ENTITY).apply(filters);
	}

	@Override
	@PostFilter(CAN_READ_OBJECT_FILTER)
	public List<UserFilter> getFiltersById(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return filterRepository.findAllByIdInAndProjectId(Lists.newArrayList(ids), projectDetails.getProjectId());
	}
}
