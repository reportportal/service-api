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

package com.epam.ta.reportportal.core.organization;

import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.OrgUserProjectPage;
import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * This is an interface for handling of organization users from the database.
 */
public interface OrganizationUsersHandler {

  OrganizationUsersPage getOrganizationUsers(Queryable filter, Pageable pageable);

  UserAssignmentResponse assignUser(Long orgId, OrgUserAssignment request);


  /**
   * Unassigns a user from an organization.
   *
   * @param orgId          The ID of the organization.
   * @param userToUnassign The ID of the user to unassign.
   */
  void unassignUser(Long orgId, Long userToUnassign);

  OrgUserProjectPage findUserProjectsInOrganization(Long userId, Long organizationId, Pageable pageable);
}
