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

package com.epam.reportportal.core.project.patch;

import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.core.events.domain.AssignUserEvent;
import com.epam.reportportal.core.project.ProjectService;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.IdContainer;
import com.epam.reportportal.util.SecurityContextUtils;
import com.epam.reportportal.ws.converter.converters.UserConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Handler for patch operations related to project users. Extends {@link BasePatchProjectHandler} to provide
 * user-specific patch logic.
 */
@Service
@Slf4j
public class PatchProjectUsersHandler extends BasePatchProjectHandler {

  private final UserRepository userRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final ProjectRepository projectRepository;
  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Constructs a new PatchProjectUsersHandler.
   *
   * @param projectService        The project service to use.
   * @param projectUserRepository The repository for project users.
   * @param objectMapper          The object mapper for JSON conversion.
   */
  @Autowired
  protected PatchProjectUsersHandler(
      UserRepository userRepository,
      ProjectService projectService,
      ProjectUserRepository projectUserRepository,
      OrganizationUserRepository organizationUserRepository,
      ObjectMapper objectMapper,
      OrganizationRepositoryCustom organizationRepository, ProjectRepository projectRepository,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    super(projectService, objectMapper);
    this.userRepository = userRepository;
    this.projectUserRepository = projectUserRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.organizationRepository = organizationRepository;
    this.projectRepository = projectRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void add(PatchOperation operation, Long orgId, Long projectId) {
    UserProjectInfo userPrjInfo;
    try {
      userPrjInfo = objectMapper.readValue(
          valueToString(operation.getValue()),
          UserProjectInfo.class
      );
      if (Objects.isNull(userPrjInfo.getId())) {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required");
      }
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Invalid field 'value'");
    }

    var principal = SecurityContextUtils.getPrincipal();

    expect(userPrjInfo.getId(), not(isEqual(principal.getUserId())))
        .verify(ErrorType.ACCESS_DENIED, "Self project role change is not allowed");

    var user = userRepository.findById(userPrjInfo.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userPrjInfo.getId()));

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    validateUserAssignment(user, org, projectId);

    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(userPrjInfo.getId(), orgId)
        .orElseGet(() -> {
          OrganizationUser organizationUser = new OrganizationUser();
          organizationUser.setOrganization(org);
          organizationUser.setUser(user);
          organizationUser.setOrganizationRole(OrganizationRole.MEMBER);
          organizationUserRepository.save(organizationUser);
          log.info("User with ID {} has been added to organization with ID {} with role MEMBER",
              userPrjInfo.getId(), orgId);
          applicationEventPublisher.publishEvent(
              new AssignUserEvent(
                  UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
                  principal.getUserId(), principal.getUsername(), orgId
              ));
          return organizationUser;
        });

    var projectRole = orgUser.getOrganizationRole().equals(OrganizationRole.MANAGER)
        ? ProjectRole.EDITOR
        : ProjectRole.valueOf(userPrjInfo.getProjectRole().getValue());

    var projectUser = new ProjectUser()
        .withUser(user)
        .withProject(project)
        .withProjectRole(projectRole);

    projectUserRepository.save(projectUser);

    log.info("User with ID {} has been assigned to project with ID {} with role {}",
        userPrjInfo.getId(), projectId, projectRole);

    applicationEventPublisher.publishEvent(
        new AssignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, projectId),
            principal.getUserId(), principal.getUsername(), orgId
        )
    );
  }

  @Override
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    List<UserProjectInfo> operationValues;
    try {
      operationValues = objectMapper.readValue(
          valueToString(operation.getValue()),
          new com.fasterxml.jackson.core.type.TypeReference<>() {
          }
      );

    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Invalid field 'value'");
    }
    operationValues.forEach(userPrjInfo -> replaceProjectUserRole(orgId, projectId, userPrjInfo));
  }

  private void replaceProjectUserRole(Long orgId, Long projectId, UserProjectInfo userPrjInfo) {
    expect(userPrjInfo.getId(), not(isEqual(SecurityContextUtils.getPrincipal().getUserId())))
        .verify(ErrorType.ACCESS_DENIED, "Self project role change is not allowed");

    var ou = organizationUserRepository.findByUserIdAndOrganization_Id(userPrjInfo.getId(), orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userPrjInfo.getId()));
    ProjectRole newRole = ou.getOrganizationRole().equals(OrganizationRole.MANAGER) ? ProjectRole.EDITOR :
        ProjectRole.valueOf(userPrjInfo.getProjectRole().getValue());

    projectUserRepository.findProjectUserByUserIdAndProjectId(userPrjInfo.getId(), projectId)
        .ifPresentOrElse(pru -> pru.setProjectRole(newRole), () -> {
          throw new ReportPortalException(ErrorType.USER_NOT_FOUND, userPrjInfo.getId());
        });
  }

  @Override
  public void remove(PatchOperation operation, Long orgId, Long projectId) {
    if (ObjectUtils.isEmpty(operation.getValue())) {
      unassignAllUsersFromProject(projectId);
      return;
    }
    List<IdContainer> ids;
    try {
      ids = objectMapper.readValue(
          valueToString(operation.getValue()),
          new com.fasterxml.jackson.core.type.TypeReference<>() {
          });
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Invalid field 'value'");
    }

    if (CollectionUtils.isEmpty(ids)) {
      unassignAllUsersFromProject(projectId);
      return;
    }
    ids.forEach(idContainer -> {
      projectUserRepository.findProjectUserByUserIdAndProjectId(idContainer.getId(), projectId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, idContainer));
      projectUserRepository.deleteByUserIdAndProjectIds(idContainer.getId(), List.of(projectId));
      log.info("User with ID {} has been removed from project with ID {}", idContainer.getId(), projectId);
    });
  }

  private void unassignAllUsersFromProject(Long projectId) {
    projectUserRepository.deleteAllByProjectId(projectId);
    log.info("All users have been removed from project with ID {}", projectId);
  }

  private void validateUserAssignment(User user, Organization org, Long prjId) {
    if (OrganizationType.EXTERNAL.equals(org.getOrganizationType()) && UserType.UPSA.equals(user.getUserType())) {
      throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
          "Cannot assign UPSA user to project under external organization"
      );
    }
    projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), prjId)
        .ifPresent(ignored -> {
          throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, user.getId());
        });
  }
}
