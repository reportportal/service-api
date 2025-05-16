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

package com.epam.ta.reportportal.core.organization;

import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.User;
import org.springframework.stereotype.Service;

/**
 * Service for managing user-organization relationships in the Report Portal system. Provides functionality for creating and managing user assignments
 * within organizations, including role management and persistence operations.
 */
@Service
public class OrganizationUserService {

  /**
   * Repository for performing CRUD operations on organization user relationships. Handles persistence of user-organization assignments and their
   * roles.
   */
  private final OrganizationUserRepository organizationUserRepository;

  /**
   * Constructs a new OrganizationUserService instance.
   *
   * @param organizationUserRepository the repository for managing organization user entities
   */
  public OrganizationUserService(OrganizationUserRepository organizationUserRepository) {
    this.organizationUserRepository = organizationUserRepository;
  }

  /**
   * Creates and persists a new organization user relationship.
   *
   * @param organization the organization to which the user will be assigned
   * @param assignedUser the user being assigned to the organization
   * @param role         the role string that will be converted to an OrganizationRole enum value
   * @return the persisted OrganizationUser entity representing the user-organization relationship
   */
  public OrganizationUser saveOrganizationUser(Organization organization, User assignedUser, String role) {
    var organizationUser = new OrganizationUser();
    organizationUser.setOrganization(organization);
    organizationUser.setUser(assignedUser);
    organizationUser.setOrganizationRole(OrganizationRole.valueOf(role));
    return organizationUserRepository.save(organizationUser);
  }
}
