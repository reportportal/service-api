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

package com.epam.reportportal.base.core.organization;

import com.epam.reportportal.base.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.base.core.project.ProjectUserService;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service for managing user-organization relationships. Handles persistence and cascade operations without event
 * publishing. Events should be published by the calling handler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationUserService {

  /**
   * Repository for performing CRUD operations on organization user relationships. Handles persistence of
   * user-organization assignments and their roles.
   */
  private final OrganizationUserRepository organizationUserRepository;
  private final ProjectUserService projectUserService;
  private final ApplicationEventPublisher eventPublisher;
  private final UserService userService;


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

  /**
   * Removes single user from organization. Used by DELETE /organizations/{org_id}/users/{user_id}. Publishes individual
   * UnassignUserEvent.
   */
  public void removeOrganizationUserEntry(OrganizationUser organizationUser, ReportPortalUser principal) {
    var orgId = organizationUser.getOrganization().getId();
    var userId = organizationUser.getUser().getId();

    organizationUserRepository.delete(organizationUser);

    eventPublisher.publishEvent(new UnassignUserEvent(
        UserConverter.TO_ACTIVITY_RESOURCE.apply(organizationUser.getUser(), null),
        principal.getUserId(),
        principal.getUsername(),
        orgId
    ));

    projectUserService.unassignUserFromProjectsByOrgId(orgId, userId, principal);
  }

  /**
   * Bulk delete specific users. Used by PATCH remove operation. Does NOT publish events - caller handles bulk events.
   */
  public void deleteByUserIdsAndOrganizationId(List<Long> userIds, Long orgId) {
    organizationUserRepository.deleteByOrganizationIdAndUserIdIn(orgId, userIds);
    projectUserService.unassignUsersFromProjectsByOrgId(orgId, userIds);
  }

  /**
   * Bulk delete users not in list. Used by PATCH replace operation. Does NOT publish events - caller handles bulk
   * events.
   */
  public List<Long> deleteByOrganizationIdAndUserIdNotIn(Long orgId, List<Long> userIdsToKeep) {
    var removedUserIds = organizationUserRepository.deleteByOrganizationIdAndUserIdNotIn(orgId, userIdsToKeep);
    projectUserService.unassignUsersFromProjectsByOrgId(orgId, removedUserIds);
    return removedUserIds;
  }

  /**
   * Bulk delete all users.
   */
  public void unassignAllUsersFromOrganization(Long orgId) {
    var removedUserIds = organizationUserRepository.unassignAllUsersByOrgId(orgId);
    projectUserService.unassignUsersFromProjectsByOrgId(orgId, removedUserIds);
  }
}
