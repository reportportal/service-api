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
import static com.epam.ta.reportportal.util.OffsetUtils.withOffsetData;

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

  /**
   * This method returns a page of projects for a particular organization based on the provided
   * filter and pagination details.
   *
   * @param user     the {@link ReportPortalUser} whose details are used to apply the additional
   *                 filters
   * @param orgId    the id of the organization whose projects are queried
   * @param filter   the {@link Filter} with condition(s) to be applied on the project querying
   * @param pageable the {@link Pageable} to define the pagination details for the result
   * @return an {@link OrganizationProjectsPage} representing a page of projects for the provided
   * organization
   */
  @Override
  public OrganizationProjectsPage getOrganizationProjectsList(ReportPortalUser user, Long orgId,
      Filter filter, Pageable pageable) {
    addOrganizationMemberFilter(user, filter, orgId);
    Page<ProjectProfile> projectProfileList =
        organizationProjectRepository.getProjectProfileListByFilter(filter, pageable);

    OrganizationProjectsPage organizationProjectsPage =
        new OrganizationProjectsPage()
            .items(projectProfileList.getContent());

    return withOffsetData(organizationProjectsPage, projectProfileList);
  }


  /**
   * This method modifies the given filter based on the user's role. If the user is NOT an
   * ADMINISTRATOR and their role in the organization is MEMBER, the filter is modified to add a
   * condition that filters by projects that the user is part of.
   *
   * @param user   the {@link ReportPortalUser} logged-in user info
   * @param filter the {@link Filter} that is to be modified
   * @param orgId  the id of the organization that the user is part of
   */
  private void addOrganizationMemberFilter(ReportPortalUser user, Filter filter, Long orgId) {
    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)
        && user.getOrganizationDetails().get(orgId.toString()).getOrgRole()
        .equals(OrganizationRole.MEMBER)) {

      var projectIds = projectUserRepository.findProjectIdsByUserId(orgId)
          .stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));
      if (!projectIds.isEmpty()) {
        filter.withCondition(
            new FilterCondition(Condition.IN, false, projectIds, CRITERIA_PROJECT_ID));
      }
    }
  }
}
