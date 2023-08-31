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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class ProjectExtractor {

  private final ProjectUserRepository projectUserRepository;

  @Autowired
  public ProjectExtractor(ProjectUserRepository projectUserRepository) {
    this.projectUserRepository = projectUserRepository;
  }

  /**
   * Extracts project details for specified user by specified project name
   *
   * @param user        User
   * @param projectName Project name
   * @return Project Details
   */
  public ReportPortalUser.ProjectDetails extractProjectDetails(ReportPortalUser user,
      String projectName) {
    final String normalizedProjectName = normalizeId(projectName);

    if (user.getUserRole().equals(ADMINISTRATOR)) {
      return extractProjectDetailsAdmin(user, projectName);
    }
    return user.getProjectDetails().computeIfAbsent(normalizedProjectName,
        k -> findProjectDetails(user, normalizedProjectName).orElseThrow(
            () -> new ReportPortalException(ErrorType.ACCESS_DENIED,
                "Please check the list of your available projects."
            ))
    );
  }

  /**
   * Find project details for specified user by specified project name
   *
   * @param user        User
   * @param projectName Project name
   * @return {@link Optional} with Project Details
   */
  public Optional<ReportPortalUser.ProjectDetails> findProjectDetails(ReportPortalUser user,
      String projectName) {
    return projectUserRepository.findDetailsByUserIdAndProjectName(user.getUserId(), projectName);
  }

  /**
   * Extracts project details for specified user by specified project name If user is ADMINISTRATOR
   * - he is added as a PROJECT_MANAGER to the project
   *
   * @param user        User
   * @param projectName Project name
   * @return Project Details
   */
  public ReportPortalUser.ProjectDetails extractProjectDetailsAdmin(ReportPortalUser user,
      String projectName) {

    //dirty hack to allow everything for user with 'admin' authority
    if (user.getUserRole().getAuthority().equals(ADMINISTRATOR.getAuthority())) {
      ReportPortalUser.ProjectDetails projectDetails = projectUserRepository.findAdminDetailsProjectName(
              normalizeId(projectName))
          .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
      user.getProjectDetails().put(
          normalizeId(projectName),
          projectDetails
      );
    }

    return Optional.ofNullable(user.getProjectDetails().get(normalizeId(projectName))).orElseThrow(
        () -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "Please check the list of your available projects."
        ));
  }

}
