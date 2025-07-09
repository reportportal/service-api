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

package com.epam.ta.reportportal.core.user;

import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.InstanceUserPage;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.UserResource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * @author Andrei_Ramanchuk
 */
public interface GetUserHandler {

  /**
   * Get specified user info
   *
   * @param username    Username
   * @param currentUser Logged-in username
   * @return {@link UserResource}
   */
  UserResource getUser(String username, ReportPortalUser currentUser);

  /**
   * Get logged-in user info
   *
   * @param currentUser Logged-in username
   * @return {@link UserResource}
   */
  UserResource getUser(ReportPortalUser currentUser);

  /**
   * Get logged-in user info
   *
   * @param currentUser Logged-in username
   * @return {@link UserResource}
   */
  InstanceUser getCurrentUser(ReportPortalUser currentUser);


  /**
   * Validate existence of username or email
   *
   * @param username User name
   * @param email    email
   * @return {@link YesNoRS}
   */
  YesNoRS validateInfo(String username, String email);

  /**
   * Get all users by filter with paging
   *
   * @param filter         Filter
   * @param pageable       Paging
   * @param membershipDetails Membership details
   * @return Page of users
   */
  Page<UserResource> getUsers(Filter filter, Pageable pageable,
      MembershipDetails membershipDetails);

  Map<String, UserResource.AssignedProject> getUserProjects(String userName);

  /**
   * Get page of users with filter
   *
   * @param filter   Filter
   * @param pageable Paging
   * @return Page of {@link UserResource}
   */
  Page<UserResource> getAllUsers(Queryable filter, Pageable pageable);

  /**
   * Get page of users with filter
   *
   * @param filter   Filter
   * @param pageable Paging
   * @param excludeFields fields to exclude from response
   * @return Page of {@link UserResource}
   */
  InstanceUserPage getUsersExcluding(Queryable filter, Pageable pageable, String... excludeFields);

  /**
   * Export Users info according to the {@link ReportFormat} type
   *
   * @param reportFormat {@link ReportFormat}
   * @param filter       {@link Filter}
   * @param outputStream {@link HttpServletResponse#getOutputStream()}
   */
  void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter);


  /**
   * Export users info according to the {@link ReportFormat} type with pagination.
   *
   * @param reportFormat {@link ReportFormat} format for export
   * @param outputStream {@link OutputStream} to write exported data
   * @param filter       {@link Queryable} filter for users
   * @param pageable     {@link Pageable} pagination information
   */
  void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter, Pageable pageable);

  Page<UserResource> searchUsers(String term, Pageable pageable);
}
