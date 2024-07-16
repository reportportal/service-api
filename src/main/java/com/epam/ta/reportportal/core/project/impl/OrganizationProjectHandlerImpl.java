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

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;

import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.ProjectProfile;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.project.OrganizationProjectRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrganizationProjectHandlerImpl implements OrganizationProjectHandler {

  private final OrganizationProjectRepository organizationProjectRepository;
  private final ProjectUserRepository projectUserRepository;

  public OrganizationProjectHandlerImpl(OrganizationProjectRepository organizationProjectRepository,
      ProjectUserRepository projectUserRepository) {
    this.organizationProjectRepository = organizationProjectRepository;
    this.projectUserRepository = projectUserRepository;
  }

  @Override
  public OrganizationProjectsPage getOrganizationProjectsList(ReportPortalUser user, Long orgId,
      Filter filter, Pageable pageable) {
    OrganizationProjectsPage organizationProjectsPage = new OrganizationProjectsPage();

    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)
        && user.getOrganizationDetails().get(orgId.toString()).getOrgRole()
        .equals(OrganizationRole.MEMBER)) {

      var projectIds = projectUserRepository.findProjectIdsByUserId(user.getUserId())
          .stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));

      if (projectIds.isEmpty()) {
        // return empty response
        return responseWithPageParameters(organizationProjectsPage, pageable, 0);
      } else {
        filter.withCondition(
            new FilterCondition(Condition.IN, false, projectIds, CRITERIA_PROJECT_ID));
      }
    }

    Page<ProjectProfile> projectProfilePagedList =
        organizationProjectRepository.getProjectProfileListByFilter(filter, pageable);
    organizationProjectsPage.items(projectProfilePagedList.getContent());

    return responseWithPageParameters(organizationProjectsPage, pageable,
        projectProfilePagedList.getTotalElements());
  }


}
