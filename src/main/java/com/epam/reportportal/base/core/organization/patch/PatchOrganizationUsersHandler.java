/*
 * Copyright 2026 EPAM Systems
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
import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.core.events.domain.UnassignUserEvent;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.IdContainer;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Handler for patch operations related to organization users.
 * Extends {@link BasePatchOrganizationHandler} to provide user-specific patch logic.
 */
@Service
@Slf4j
public class PatchOrganizationUsersHandler extends BasePatchOrganizationHandler {

  private final UserRepository userRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Constructor.
   *
   * @param organizationUserService The service for managing organization users.
   * @param objectMapper            The object mapper for JSON conversion.
   */
  @Autowired
  protected PatchOrganizationUsersHandler(
      OrganizationUserService organizationUserService,
      ObjectMapper objectMapper,
      UserRepository userRepository,
      OrganizationRepositoryCustom organizationRepository,
      OrganizationUserRepository organizationUserRepository,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    super(organizationUserService, objectMapper);
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.applicationEventPublisher = applicationEventPublisher;
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
    var ops = readOperationValue(operation, new TypeReference<List<UserOrgInfo>>() {
    });

    var newUserIds = ops.stream()
        .map(UserOrgInfo::getId)
        .toList();

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    if (newUserIds.isEmpty()) {
      unassignAllUsersFromOrganization(org.getId());
      return;
    } else {
      // TODO: Add OrgUser Activity Resource or extend OrganizationAttributesActivityResource
      // for publishing events when several users are unassigned from organization
      organizationUserRepository.deleteByOrganizationIdAndUserIdNotIn(org.getId(), newUserIds);
      log.info("Users not in {} have been removed from organization with ID {}", newUserIds, org.getId());
    }

    var principal = SecurityContextUtils.getPrincipal();

    ops.forEach(info -> {
      var userId = Optional.ofNullable(info.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

      organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId).ifPresent(_ -> {
        throw new ReportPortalException(ErrorType.USER_ALREADY_ASSIGNED, userId, orgId);
      });

      var user = userRepository.findById(userId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

      var role = Optional.ofNullable(info.getOrgRole())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'orgRole' is required"));

      organizationUserService.saveOrganizationUser(org, user, role.toString());

      log.info("User with ID {} has been assigned to the organization {} with role {}", user.getId(), orgId, role);

      applicationEventPublisher.publishEvent(
          new AssignUserEvent(
              UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
              principal.getUserId(), principal.getUsername(), org.getId()
          ));
    });
  }

  @Override
  public void remove(PatchOperation operation, Long orgId) {
    if (ObjectUtils.isEmpty(operation.getValue())) {
      unassignAllUsersFromOrganization(orgId);
      return;
    }
    var ids = readOperationValue(operation, new TypeReference<List<IdContainer>>() {
    });

    if (CollectionUtils.isEmpty(ids)) {
      unassignAllUsersFromOrganization(orgId);
      return;
    }

    var principal = SecurityContextUtils.getPrincipal();

    ids.forEach(idContainer -> {
      var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(idContainer.getId(), orgId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, idContainer.getId()));
      organizationUserRepository.deleteByUserIdAndOrganizationId(idContainer.getId(), orgId);
      log.info("User with ID {} has been removed from organization with ID {}", idContainer.getId(), orgId);
      applicationEventPublisher.publishEvent(
          new UnassignUserEvent(
              UserConverter.TO_ACTIVITY_RESOURCE.apply(orgUser.getUser(), null),
              principal.getUserId(), principal.getUsername(), orgId
          ));
    });
  }

  // TODO: Add OrgUser Activity Resource or extend OrganizationAttributesActivityResource
  // for publishing events when several users are unassigned from organization
  private void unassignAllUsersFromOrganization(Long orgId) {
    organizationUserRepository.deleteAllByOrganizationId(orgId);
    log.info("All users have been removed from organization with ID {}", orgId);
  }
}
