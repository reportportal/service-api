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

import static com.epam.reportportal.base.util.SecurityContextUtils.getPrincipal;

import com.epam.reportportal.base.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.base.core.project.ProjectUserService;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
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
 * Service for managing user-organization relationships in the Report Portal system. Provides functionality for creating
 * and managing user assignments within organizations, including role management and persistence operations.
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
  private final UserRepository userRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ProjectUserService projectUserService;
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


  public void removeOrganizationUserEntry(OrganizationUser organizationUser) {
    long orgId = organizationUser.getOrganization().getId();
    long userId = organizationUser.getUser().getId();
    organizationUserRepository.delete(organizationUser);

    sendUnassignFromOrgEvent(userId, orgId);
    projectUserService.unassignUserFromProjectsByOrgId(orgId, userId);
  }


  public void deleteByOrganizationIdAndUserIdNotIn(Long orgId, List<Long> newUserIds) {
    var unassignedUsers = organizationUserRepository.deleteByOrganizationIdAndUserIdNotIn(orgId, newUserIds);
    newUserIds.forEach(userId -> projectUserService.unassignUserFromProjectsByOrgId(orgId, userId));

    log.info("Users in {} have been removed from organization with ID {}", unassignedUsers, orgId);
  }

  public void deleteByUserIdAndOrganizationId(Long userId, Long orgId) {
    organizationUserRepository.deleteByUserIdAndOrganizationId(userId, orgId);
    sendUnassignFromOrgEvent(userId, orgId);

    projectUserService.unassignUserFromProjectsByOrgId(orgId, userId);

    log.info("User with ID {} has been removed from organization with ID {}", userId, orgId);
  }

  public void unassignAllUsersFromOrganization(Long orgId) {
    organizationUserRepository.unassignAllUsersByOrgId(orgId)
        .forEach(unassignedUserId -> {
          sendUnassignFromOrgEvent(unassignedUserId, orgId);
          projectUserService.unassignUserFromProjectsByOrgId(orgId, unassignedUserId);
        });
    log.info("All users have been removed from organization with ID {}", orgId);

  }


  private void sendUnassignFromOrgEvent(Long userId, Long orgId) {
    User user = userService.findById(userId);
    applicationEventPublisher.publishEvent(
        new UnassignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
            getPrincipal().getUserId(),
            getPrincipal().getUsername(),
            orgId
        ));
  }

}
