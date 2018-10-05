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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.acl.ReportPortalAclService;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.IShareUserFilterHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ShareUserFilterHandlerImpl implements IShareUserFilterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareUserFilterHandlerImpl.class);


    @Autowired
    private ReportPortalAclService aclService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserFilterRepository userFilterRepository;


    @Override
    public void shareFilter(String projectName, Long filterId) {
        UserFilter filter = userFilterRepository.getOne(filterId);
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND));
        project.getUsers()
            .forEach(user -> aclService.addReadPermissions(filter, user.getUser().getLogin()));
    }

    @Override
    public Iterable<UserFilterResource> getSharedFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user) {
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND));
        Page<UserFilter> filters = userFilterRepository
            .getSharedFilters(project.getId(), filter, pageable, user.getUsername());
        return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE)
            .apply(filters);
    }

    @Override
    public Iterable<UserFilterResource> getAllFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user) {
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND));
        Page<UserFilter> filters = userFilterRepository
            .getPermittedFilters(project.getId(), filter, pageable,
                user.getUsername());
        return PagedResourcesAssembler.pageConverter(UserFilterConverter.TO_FILTER_RESOURCE)
            .apply(filters);
    }
}
