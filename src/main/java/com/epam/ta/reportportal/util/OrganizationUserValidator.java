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

package com.epam.ta.reportportal.util;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;

/**
 * Validator utility class for organization user operations. Provides methods to validate user types, roles and permissions within organizations.
 */
public class OrganizationUserValidator {

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private OrganizationUserValidator() {
  }


  /**
   * Validates if the user type is compatible with the organization type. Prevents UPSA users from being assigned to external organizations.
   *
   * @param organization the organization to validate against
   * @param assignedUser the user being assigned to the organization
   * @throws ReportPortalException with ACCESS_DENIED error if validation fails
   */
  public static void validateUserType(Organization organization, User assignedUser) {
    if (organization.getOrganizationType().equals(OrganizationType.EXTERNAL)
        && assignedUser.getUserType().equals(UserType.UPSA)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED);
    }
  }

  /**
   * Validates if the user has sufficient permissions for organization operations. Access is granted for administrators, organization managers or the
   * user themselves.
   *
   * @param user             the user attempting the operation
   * @param organizationUser the organization user being operated on
   * @throws ReportPortalException with ACCESS_DENIED error if validation fails
   */
  public static void validateUserRoles(ReportPortalUser user, OrganizationUser organizationUser) {
    if (user.getUserId().equals(organizationUser.getUser().getId())
        || user.getUserRole().equals(UserRole.ADMINISTRATOR)
        || isManager(user, organizationUser)) {
      return;
    }
    throw new ReportPortalException(ErrorType.ACCESS_DENIED);
  }

  /**
   * Checks if the given user has a manager role in the specified organization.
   *
   * @param user             the user to check
   * @param organizationUser the organization user context
   * @return true if the user has a manager role, false otherwise
   */
  public static boolean isManager(ReportPortalUser user, OrganizationUser organizationUser) {
    return user.getOrganizationDetails().get(organizationUser.getOrganization().getId().toString()).getOrgRole()
        .equals(OrganizationRole.MANAGER);
  }
}
