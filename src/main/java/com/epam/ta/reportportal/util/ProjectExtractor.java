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

package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.dao.GroupMembershipRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for extracting project details for a specified user by project name. This service
 * provides methods to extract project details for users, including special handling for
 * administrators.
 *
 * @author Pavel Bortnik
 */
@Service
public class ProjectExtractor {

  private final ProjectUserRepository projectUserRepository;
  private final GroupMembershipRepository groupMembershipRepository;

  /**
   * Constructor for ProjectExtractor.
   *
   * @param projectUserRepository  ProjectUserRepository
   * @param groupMembershipRepository GroupMembershipRepository
   */
  @Autowired
  public ProjectExtractor(
      ProjectUserRepository projectUserRepository,
      GroupMembershipRepository groupMembershipRepository
  ) {
    this.projectUserRepository = projectUserRepository;
    this.groupMembershipRepository = groupMembershipRepository;
  }

  /**
   * Extracts project details for specified user by specified project name.
   *
   * @param user       User
   * @param projectKey Project name
   * @return Project Details
   */
  public MembershipDetails extractMembershipDetails(ReportPortalUser user,
      String projectKey) {

    final String normalizedProjectKey = normalizeId(projectKey);
    if (user.getUserRole().equals(ADMINISTRATOR)) {
      return extractProjectDetailsAdmin(user, projectKey);
    }
    return findMembershipDetails(user, normalizedProjectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "Please check the list of your available projects."
        ));
  }

  /**
   * Find project details for specified user by specified project name.
   *
   * @param user       User
   * @param projectKey Project unique key
   * @return {@link Optional} with Project Details found in ProjectUserRepository or GroupRepository
   */
  public Optional<MembershipDetails> findMembershipDetails(ReportPortalUser user,
      String projectKey) {
    return projectUserRepository.findDetailsByUserIdAndProjectKey(user.getUserId(), projectKey);
  }

  /**
   * Extracts project details for specified user by specified project name If user is ADMINISTRATOR
   * - he is added as a PROJECT_MANAGER to the project.
   *
   * @param user       User
   * @param projectKey Project unique key
   * @return Project Details
   */
  public MembershipDetails extractProjectDetailsAdmin(ReportPortalUser user,
      String projectKey) {
    final String normalizedProjectKey = normalizeId(projectKey);
    MembershipDetails membershipDetails =
        projectUserRepository
          .findAdminDetailsProjectKey(normalizeId(normalizedProjectKey))
            .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));
    return membershipDetails;
  }


}
