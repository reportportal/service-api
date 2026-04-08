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

package com.epam.reportportal.base.core.organization.patch;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.UserOrgInfo;
import com.epam.reportportal.base.core.events.domain.OrganizationUsersUpdatedEvent;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.IdContainer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Handler for patch operations related to organization users. Supports bulk assign/unassign operations with
 * corresponding bulk events.
 */
@Service
public class PatchOrganizationUsersHandler extends BasePatchOrganizationHandler {

  private final UserRepository userRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final ApplicationEventPublisher eventPublisher;


  /**
   * Constructor.
   *
   * @param organizationUserService The service for managing organization users.
   * @param objectMapper            The object mapper for JSON conversion.
   */
  protected PatchOrganizationUsersHandler(
      OrganizationUserService organizationUserService,
      ObjectMapper objectMapper,
      UserRepository userRepository,
      OrganizationRepositoryCustom organizationRepository,
      OrganizationUserRepository organizationUserRepository,
      ApplicationEventPublisher eventPublisher) {
    super(organizationUserService, objectMapper);
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Add operation according to RFC 6902 is treated as replace for the list of users.
   */
  @Override
  public void add(PatchOperation operation, Long orgId) {
    replace(operation, orgId);
  }

  @Override
  public void replace(PatchOperation operation, Long orgId) {
    var userOrgInfos = readOperationValue(operation, new TypeReference<List<UserOrgInfo>>() {
    });
    validateUserOrgInfos(userOrgInfos);

    var principal = getPrincipal();
    var org = findOrganizationOrThrow(orgId);

    var beforeUserIds = organizationUserRepository.findUserIdsByOrganizationId(orgId);

    if (CollectionUtils.isEmpty(userOrgInfos)) {
      organizationUserService.unassignAllUsersFromOrganization(orgId);
      publishEvent(principal, org, beforeUserIds, List.of(), EventAction.UNASSIGN);
      return;
    }

    var newUserIds = userOrgInfos.stream()
        .map(UserOrgInfo::getId)
        .toList();

    // Remove users not in new list
    var removedUserIds = organizationUserService.deleteByOrganizationIdAndUserIdNotIn(orgId, newUserIds);

    var afterRemovalUserIds = beforeUserIds.stream()
        .filter(id -> !removedUserIds.contains(id))
        .toList();

    publishEvent(principal, org, beforeUserIds, afterRemovalUserIds, EventAction.UNASSIGN);

    // Process assignments
    var assignedUserIds = processUserAssignments(userOrgInfos, org);

    if (!assignedUserIds.isEmpty()) {
      var afterAssignUserIds = new ArrayList<>(afterRemovalUserIds);
      afterAssignUserIds.addAll(assignedUserIds);
      publishEvent(principal, org, afterRemovalUserIds, afterAssignUserIds, EventAction.ASSIGN);
    }
  }

  @Override
  public void remove(PatchOperation operation, Long orgId) {
    var principal = getPrincipal();
    var org = findOrganizationOrThrow(orgId);

    var beforeUserIds = organizationUserRepository.findUserIdsByOrganizationId(orgId);
    var userIdsToRemove = extractUserIdsToRemove(operation);

    if (CollectionUtils.isEmpty(userIdsToRemove)) {
      organizationUserService.unassignAllUsersFromOrganization(orgId);
      publishEvent(principal, org, beforeUserIds, List.of(), EventAction.UNASSIGN);
      return;
    }

    validateUsersExistInOrganization(userIdsToRemove, orgId);
    organizationUserService.deleteByUserIdsAndOrganizationId(userIdsToRemove, orgId);

    var afterUserIds = beforeUserIds.stream()
        .filter(id -> !userIdsToRemove.contains(id))
        .toList();

    publishEvent(principal, org, beforeUserIds, afterUserIds, EventAction.UNASSIGN);
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private Organization findOrganizationOrThrow(Long orgId) {
    return organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
  }

  private List<Long> extractUserIdsToRemove(PatchOperation operation) {
    if (operation.getValue() == null) {
      return List.of();
    }
    var ids = readOperationValue(operation, new TypeReference<List<IdContainer>>() {
    });
    if (CollectionUtils.isEmpty(ids)) {
      return List.of();
    }
    return ids.stream()
        .map(IdContainer::getId)
        .filter(Objects::nonNull)
        .toList();
  }

  private List<Long> processUserAssignments(List<UserOrgInfo> userOrgInfos, Organization org) {
    var userIds = userOrgInfos.stream()
        .map(info -> Optional.ofNullable(info.getId())
            .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required")))
        .toList();
    if (CollectionUtils.isEmpty(userIds)) {
      return List.of();
    }

    var usersById = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

    var existingOrgUsersById = organizationUserRepository
        .findAllByOrganizationIdAndUserIdIn(org.getId(), userIds)
        .stream()
        .collect(Collectors.toMap(ou -> ou.getUser().getId(), Function.identity()));

    var orgUsersToSave = new ArrayList<OrganizationUser>();
    var newlyAssignedUserIds = new ArrayList<Long>();

    for (var info : userOrgInfos) {
      var role = Optional.ofNullable(info.getOrgRole())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'orgRole' is required"));

      var user = Optional.ofNullable(usersById.get(info.getId()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, info.getId()));

      var orgRole = OrganizationRole.valueOf(role.toString());
      var existingOrgUser = existingOrgUsersById.get(user.getId());

      if (existingOrgUser == null) {
        var newOrgUser = new OrganizationUser();
        newOrgUser.setOrganization(org);
        newOrgUser.setUser(user);
        newOrgUser.setOrganizationRole(orgRole);
        orgUsersToSave.add(newOrgUser);
        newlyAssignedUserIds.add(user.getId());
      } else if (!existingOrgUser.getOrganizationRole().equals(orgRole)) {
        existingOrgUser.setOrganizationRole(orgRole);
        orgUsersToSave.add(existingOrgUser);
      }
    }

    if (!orgUsersToSave.isEmpty()) {
      organizationUserRepository.saveAll(orgUsersToSave);
    }

    return newlyAssignedUserIds;
  }

  private void validateUserOrgInfos(List<UserOrgInfo> userOrgInfos) {
    if (CollectionUtils.isEmpty(userOrgInfos)) {
      return;
    }
    for (var info : userOrgInfos) {
      if (info.getId() == null) {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required");
      }
      if (info.getOrgRole() == null) {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'orgRole' is required");
      }
    }
  }

  private void validateUsersExistInOrganization(List<Long> userIds, Long orgId) {
    var existingUserIds = organizationUserRepository.findUserIdsByOrganizationId(orgId);
    var missingUserIds = userIds.stream()
        .filter(id -> !existingUserIds.contains(id))
        .toList();

    if (!missingUserIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.USER_NOT_FOUND, missingUserIds.getFirst());
    }
  }

  private void publishEvent(ReportPortalUser principal, Organization org, List<Long> beforeUserIds,
      List<Long> afterUserIds, EventAction action) {
    if (beforeUserIds.equals(afterUserIds)) {
      return;
    }
    eventPublisher.publishEvent(new OrganizationUsersUpdatedEvent(
        principal.getUserId(), principal.getUsername(),
        org.getId(), org.getName(),
        beforeUserIds, afterUserIds, action
    ));
  }
}
