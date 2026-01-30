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
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.IdContainer;
import com.epam.reportportal.util.SecurityContextUtils;
import com.epam.reportportal.ws.converter.converters.UserConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    var userPrjInfo = readOperationValue(operation,
        new com.fasterxml.jackson.core.type.TypeReference<UserProjectInfo>() {
        });

    var userId = Optional.ofNullable(userPrjInfo.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), project.getId())
        .ifPresent(ignored -> {
          throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, user.getId());
        });

    assignUserToProject(user, org, project, ProjectRole.valueOf(userPrjInfo.getProjectRole().getValue()));
  }

  @Override
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    var prjUsersInfo = readOperationValue(operation,
        new com.fasterxml.jackson.core.type.TypeReference<List<UserProjectInfo>>() {
        });

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    unassignAllUsersFromProject(project.getId());

    prjUsersInfo.forEach(info -> {
      var userId = Optional.ofNullable(info.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

      var user = userRepository.findById(userId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

      assignUserToProject(user, org, project, ProjectRole.valueOf(info.getProjectRole().getValue()));
    });
  }

  private void assignUserToProject(User user, Organization org, Project project, ProjectRole newRole) {
    var principal = SecurityContextUtils.getPrincipal();

    validateUserAssignment(user, org);

    var orgUser = organizationUserRepository.findByUserIdAndOrganization_Id(user.getId(), org.getId())
        .orElseGet(() -> {
          OrganizationUser organizationUser = new OrganizationUser();
          organizationUser.setOrganization(org);
          organizationUser.setUser(user);
          organizationUser.setOrganizationRole(OrganizationRole.MEMBER);
          organizationUserRepository.save(organizationUser);
          log.info("User with ID {} has been added to organization with ID {} with role MEMBER",
              user.getId(), org.getId());
          applicationEventPublisher.publishEvent(
              new AssignUserEvent(
                  UserConverter.TO_ACTIVITY_RESOURCE.apply(user, null),
                  principal.getUserId(), principal.getUsername(), org.getId()
              ));
          return organizationUser;
        });

    var projectRole = orgUser.getOrganizationRole().equals(OrganizationRole.MANAGER)
        ? ProjectRole.EDITOR
        : newRole;

    var projectUser = new ProjectUser()
        .withUser(user)
        .withProject(project)
        .withProjectRole(projectRole);

    projectUserRepository.save(projectUser);

    log.info("User with ID {} has been assigned to project with ID {} with role {}",
        user.getId(), project.getId(), projectRole);

    applicationEventPublisher.publishEvent(
        new AssignUserEvent(
            UserConverter.TO_ACTIVITY_RESOURCE.apply(user, project.getId()),
            principal.getUserId(), principal.getUsername(), org.getId()
        )
    );
  }

  @Override
  public void remove(PatchOperation operation, Long orgId, Long projectId) {
    if (ObjectUtils.isEmpty(operation.getValue())) {
      unassignAllUsersFromProject(projectId);
      return;
    }
    var ids = readOperationValue(operation,
        new com.fasterxml.jackson.core.type.TypeReference<List<IdContainer>>() {
        });

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

  private <T> T readOperationValue(PatchOperation operation, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
    try {
      return objectMapper.readValue(valueToString(operation.getValue()), typeRef);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Invalid field 'value': " + (e.getCause() != null ? e.getCause().getMessage() : e.getOriginalMessage()));
    }
  }

  private void unassignAllUsersFromProject(Long projectId) {
    projectUserRepository.deleteAllByProjectId(projectId);
    log.info("All users have been removed from project with ID {}", projectId);
  }

  private void validateUserAssignment(User user, Organization org) {
    expect(user.getId(), not(isEqual(SecurityContextUtils.getPrincipal().getUserId())))
        .verify(ErrorType.ACCESS_DENIED, "Self project role change is not allowed");

    if (OrganizationType.EXTERNAL.equals(org.getOrganizationType()) && UserType.UPSA.equals(user.getUserType())) {
      throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT,
          "Cannot assign UPSA user to project under external organization"
      );
    }
  }
}
