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

package com.epam.reportportal.base.core.project.patch;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Helper service for common user assignment operations in projects.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectUserAssignmentHelper {

  public record OrgUserResult(OrganizationUser orgUser, boolean newlyCreated) {

  }

  private final OrganizationUserRepository organizationUserRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Gets or creates an OrganizationUser for the given user and organization. Returns an {@link OrgUserResult}
   * indicating whether the membership was newly created.
   */
  public OrgUserResult getOrCreateOrgUser(Organization org, User user) {
    var existing = organizationUserRepository.findByUserIdAndOrganization_Id(user.getId(), org.getId());
    if (existing.isPresent()) {
      return new OrgUserResult(existing.get(), false);
    }
    try {
      OrganizationUser organizationUser = new OrganizationUser();
      organizationUser.setOrganization(org);
      organizationUser.setUser(user);
      organizationUser.setOrganizationRole(OrganizationRole.MEMBER);
      organizationUserRepository.save(organizationUser);
      log.info("User with ID {} has been added to organization with ID {} with role MEMBER",
          user.getId(), org.getId());
      return new OrgUserResult(organizationUser, true);
    } catch (DataIntegrityViolationException e) {
      log.debug("Race condition occurred while adding user with ID {} to organization with ID {}",
          user.getId(), org.getId(), e);
      return new OrgUserResult(
          organizationUserRepository.findByUserIdAndOrganization_Id(user.getId(), org.getId())
              .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND,
                  "User with ID '{}' not found in organization with ID '{}'", user.getId(), org.getId())),
          false
      );
    }
  }

  /**
   * Evaluates the project role based on organization role and requested role.
   */
  public ProjectRole evaluateProjectRole(OrganizationUser orgUser, UserProjectInfo info) {
    return orgUser.getOrganizationRole().equals(OrganizationRole.MANAGER)
        ? ProjectRole.EDITOR
        : ProjectRole.valueOf(info.getProjectRole().getValue());
  }

  /**
   * Validates that user assignment is allowed.
   */
  public void validateUserAssignment(Organization org, ReportPortalUser principal, User user) {
    if (principal.getUserRole().equals(UserRole.ADMINISTRATOR)) {
      return;
    }

    var principalIsOrgManager = organizationUserRepository
        .findByUserIdAndOrganization_Id(principal.getUserId(), org.getId())
        .map(ou -> OrganizationRole.MANAGER.equals(ou.getOrganizationRole()))
        .orElse(false);

    if (!principalIsOrgManager) {
      expect(user.getId(), not(isEqual(principal.getUserId())))
          .verify(ErrorType.ACCESS_DENIED, "Self project role change is not allowed");
    }

    if (OrganizationType.EXTERNAL.equals(org.getOrganizationType()) && UserType.UPSA.equals(user.getUserType())) {
      throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
          "Cannot assign UPSA user to project under external organization"
      );
    }
  }

  /**
   * Publishes user assignment event and logs the action.
   */
  public void publishUserAssignEvent(
      ReportPortalUser principal,
      User user,
      Long orgId,
      Long projectId,
      ProjectRole projectRole
  ) {
    log.info("User with ID {} has been assigned to project with ID {} with role {}",
        user.getId(), projectId, projectRole);

    applicationEventPublisher.publishEvent(
        new AssignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, projectId),
            principal.getUserId(), principal.getUsername(), orgId
        )
    );
  }

  /**
   * Returns all user IDs currently assigned to the given organization.
   */
  public List<Long> getOrgUserIds(Long orgId) {
    return organizationUserRepository.findUserIdsByOrganizationId(orgId);
  }

  /**
   * Publishes an organization-level user assignment event (single user).
   */
  public void publishOrgUserAssignedEvent(ReportPortalUser principal, User user, Long orgId) {
    applicationEventPublisher.publishEvent(
        new AssignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
            principal.getUserId(), principal.getUsername(), orgId
        )
    );
  }
}
