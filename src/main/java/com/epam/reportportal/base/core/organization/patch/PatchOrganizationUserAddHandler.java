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
import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.epam.reportportal.base.ws.converter.converters.UserConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Handler for patch operations related to organization user.
 * Extends {@link BasePatchOrganizationHandler} to provide user-specific patch logic.
 */
@Service
@Slf4j
public class PatchOrganizationUserAddHandler extends BasePatchOrganizationHandler {

  private final UserRepository userRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final OrganizationUserRepository organizationUserRepository;

  /**
   * Constructor.
   *
   * @param organizationUserService The service for managing organization users.
   * @param objectMapper            The object mapper for JSON conversion.
   */
  @Autowired
  protected PatchOrganizationUserAddHandler(
      UserRepository userRepository,
      OrganizationUserService organizationUserService,
      ObjectMapper objectMapper,
      OrganizationRepositoryCustom organizationRepository,
      ApplicationEventPublisher applicationEventPublisher,
      OrganizationUserRepository organizationUserRepository) {
    super(organizationUserService, objectMapper);
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public void add(PatchOperation operation, Long orgId) {
    var userOrgInfo = readOperationValue(operation, new TypeReference<UserOrgInfo>() {
    });

    var userId = Optional.ofNullable(userOrgInfo.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

    var role = Optional.ofNullable(userOrgInfo.getOrgRole())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'orgRole' is required"));

    organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId).ifPresent(_ -> {
      throw new ReportPortalException(ErrorType.USER_ALREADY_ASSIGNED, userId, orgId);
    });

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    organizationUserService.saveOrganizationUser(org, user, role.toString());

    log.info("User with ID {} has been assigned to the organization {} with role {}", user.getId(), orgId, role);

    var principal = SecurityContextUtils.getPrincipal();

    applicationEventPublisher.publishEvent(
        new AssignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
            principal.getUserId(), principal.getUsername(), org.getId()
        ));
  }
}
