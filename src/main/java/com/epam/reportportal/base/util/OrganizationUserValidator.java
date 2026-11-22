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

package com.epam.reportportal.base.util;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collection;
import java.util.Objects;

/**
 * Validator utility class for organization user operations. Provides methods to validate user types, roles and
 * permissions within organizations.
 */
public class OrganizationUserValidator {

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private OrganizationUserValidator() {
  }


  /**
   * Validates if the user type is compatible with the organization type. Prevents UPSA users from being assigned to
   * external organizations.
   *
   * @param organization the organization to validate against
   * @param assignedUser the user being assigned to the organization
   * @throws ReportPortalException with ACCESS_DENIED error if validation fails
   */
  public static void validateUserType(Organization organization, User assignedUser) {
    if (organization.getOrganizationType().equals(OrganizationType.EXTERNAL)
        && assignedUser.getUserType().equals(UserType.UPSA)) {
      throw new ReportPortalException(ErrorType.UPSA_USER_DENIED);
    }
  }

  /**
   * Validates if the user has sufficient permissions for organization operations. Access is granted for administrators,
   * organization managers or the user themselves.
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
    var orgDetails = user.getOrganizationDetails()
        .get(organizationUser.getOrganization().getId().toString());
    return orgDetails != null && OrganizationRole.MANAGER.equals(orgDetails.getOrgRole());
  }

  /**
   * Forbids removing the owner of a personal organization.
   *
   * @param organization   the organization users are removed from
   * @param removedUserIds ids of users about to be removed
   */
  public static void validatePersonalOrganizationOwnerKept(Organization organization,
      Collection<Long> removedUserIds) {
    if (OrganizationType.PERSONAL.equals(organization.getOrganizationType())
        && organization.getOwnerId() != null
        && removedUserIds.contains(organization.getOwnerId())) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "User %s cannot be unassigned from personal organization".formatted(organization.getOwnerId()));
    }
  }

  /**
   * A non-administrator manager cannot lower the role of their own account.
   *
   * @param principal       the user performing the operation
   * @param targetUserId    the user whose role is being set
   * @param existingRole    current role of the target in the organization, or null if not a member
   * @param newRole         requested role
   */
  public static void validateManagerChangingRole(ReportPortalUser principal, Long targetUserId,
      OrganizationRole existingRole, OrganizationRole newRole) {
    if (Objects.equals(principal.getUserId(), targetUserId)
        && !UserRole.ADMINISTRATOR.equals(principal.getUserRole())
        && OrganizationRole.MANAGER.equals(existingRole)
        && !OrganizationRole.MANAGER.equals(newRole)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, "Impossible to change the role of the own account");
    }
  }
}
