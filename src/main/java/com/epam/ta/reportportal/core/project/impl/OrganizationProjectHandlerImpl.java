/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.util.OffsetUtils.withOffsetData;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.dao.project.OrganizationProjectRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.api.model.OrganizationProjectsList;
import com.epam.ta.reportportal.api.model.ProjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrganizationProjectHandlerImpl implements OrganizationProjectHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      OrganizationProjectHandlerImpl.class);

  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationProjectRepository organizationProjectRepository;

  public OrganizationProjectHandlerImpl(OrganizationUserRepository organizationUserRepository,
      OrganizationProjectRepository organizationProjectRepository) {
    this.organizationUserRepository = organizationUserRepository;
    this.organizationProjectRepository = organizationProjectRepository;
  }

  @Override
  public OrganizationProjectsList getOrganizationProjectsList(ReportPortalUser user, Long orgId,
      Filter filter,
      Pageable pageable) {
    Page<ProjectProfile> projectProfileList = getProjectProfileList(user, orgId, filter, pageable);

    OrganizationProjectsList organizationProjectsList =
        new OrganizationProjectsList()
            .organizationProjectsListItems(projectProfileList.getContent());

    return withOffsetData(organizationProjectsList, projectProfileList);
  }

  Page<ProjectProfile> getProjectProfileList(ReportPortalUser user, Long orgId, Filter filter,
      Pageable pageable) {

    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)) {
      LOGGER.debug("Checking if the user '{}' is affiliated with organization '{}'",
          user.getUserId(),
          orgId);

      organizationUserRepository.findByUserIdAndOrganization_Id(user.getUserId(), orgId)
          .map(orgUser -> {
            if (orgUser.getOrganizationRole().equals(OrganizationRole.MEMBER)) {
              filter.withCondition(
                  new FilterCondition(Condition.EQUALS, false, user.getUserId().toString(),
                      "user_id"));
            }
            return filter;
          })
          .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
              "The user does not belong to the organization."));
    }
    return organizationProjectRepository.getProjectProfileListByFilter(filter, pageable);

  }
}
