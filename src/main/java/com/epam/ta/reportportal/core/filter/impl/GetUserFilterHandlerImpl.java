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
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.OwnedEntityResource;
import com.epam.ta.reportportal.model.filter.UserFilterResource;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  public UserFilterResource getUserFilter(Long id, ReportPortalUser.ProjectDetails projectDetails) {
    final UserFilter userFilter =
        filterRepository.findByIdAndProjectId(id, projectDetails.getProjectId()).orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, id,
                projectDetails.getProjectName()
            ));
    return UserFilterConverter.TO_FILTER_RESOURCE.apply(userFilter);
  }

  @Override
  public Iterable<UserFilterResource> getUserFilters(String projectName, Pageable pageable,
      Filter filter, ReportPortalUser user) {
    ReportPortalUser.ProjectDetails projectDetails =
        projectExtractor.extractProjectDetails(user, projectName);
    Page<UserFilter> userFilters =
        filterRepository.findByFilter(ProjectFilter.of(filter, projectDetails.getProjectId()),
            pageable
        );
    return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE)
        .apply(userFilters);
  }

  @Override
  public Iterable<OwnedEntityResource> getFiltersNames(
      ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
      ReportPortalUser user) {
    final Page<UserFilter> userFilters =
        filterRepository.findByFilter(ProjectFilter.of(filter, projectDetails.getProjectId()),
            pageable
        );
    return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_OWNED_ENTITY_RESOURCE)
        .apply(userFilters);
  }

  @Override
  public List<UserFilter> getFiltersById(Long[] ids, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {
    return filterRepository.findAllByIdInAndProjectId(
        Lists.newArrayList(ids), projectDetails.getProjectId());
  }
}
