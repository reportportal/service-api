/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.project;

import com.epam.reportportal.base.core.jasper.ReportFormat;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.model.project.ProjectResource;
import com.epam.reportportal.base.model.user.UserResource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * @author Andrei_Ramanchuk
 */
public interface GetProjectHandler {

  /**
   * Get project users info
   *
   * @param membershipDetails {@link MembershipDetails}
   * @param pageable          {@link Pageable}
   * @param user              {@link ReportPortalUser}
   * @return list of {@link UserResource}
   */
  Page<UserResource> getProjectUsers(MembershipDetails membershipDetails, Filter filter, Pageable pageable,
      ReportPortalUser user);

  boolean exists(Long id);

  Project get(MembershipDetails membershipDetails);

  Project get(Long id);

  Project get(String name);

  /**
   * Find project entity without fetching related entities
   *
   * @param name Project name to search
   * @return {@link Project}
   */
  Project getRaw(String name);

  /**
   * Get project resource information
   *
   * @param projectKey Project name
   * @param user       User
   * @return {@link ProjectResource}
   */
  ProjectResource getResource(String projectKey, ReportPortalUser user);

  /**
   * Get list of specified usernames
   *
   * @param membershipDetails Membership details
   * @param value             Login
   * @return List of found user logins
   */
  List<String> getUserNames(MembershipDetails membershipDetails, String value);

  /**
   * Get all project names
   *
   * @return All project names
   */
  List<String> getAllProjectNames();

  /**
   * Get all project names, which contain provided term
   *
   * @param term project term
   * @return {@link List} of the {@link Project#name}
   */
  List<String> getAllProjectNamesByTerm(String term);

  /**
   * Export Projects info according to the {@link ReportFormat} type
   *
   * @param reportFormat {@link ReportFormat}
   * @param filter       {@link Queryable}
   * @param outputStream {@link HttpServletResponse#getOutputStream()}
   */
  void exportProjects(ReportFormat reportFormat, Queryable filter, HttpServletResponse outputStream);

  Map<String, Boolean> getAnalyzerIndexingStatus();
}
