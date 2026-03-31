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

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.IdContainer;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
  private final ProjectUserAssignmentHelper assignmentHelper;

  /**
   * Constructs a new PatchProjectUsersHandler.
   *
   * @param projectService        The project service to use.
   * @param projectUserRepository The repository for project users.
   * @param objectMapper          The object mapper for JSON conversion.
   */
  @Autowired
  protected PatchProjectUsersHandler(UserRepository userRepository, ProjectService projectService,
      ProjectUserRepository projectUserRepository, ObjectMapper objectMapper,
      OrganizationRepositoryCustom organizationRepository, ProjectRepository projectRepository,
      ProjectUserAssignmentHelper assignmentHelper) {
    super(projectService, objectMapper);
    this.userRepository = userRepository;
    this.projectUserRepository = projectUserRepository;
    this.organizationRepository = organizationRepository;
    this.projectRepository = projectRepository;
    this.assignmentHelper = assignmentHelper;
  }

  /**
   * Add operation according to RFC 6902 is treated as replace for the list of users.
   */
  @Override
  public void add(PatchOperation operation, Long orgId, Long projectId) {
    replace(operation, orgId, projectId);
  }

  @Override
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    var prjUsersInfo = readOperationValue(operation, new TypeReference<List<UserProjectInfo>>() {
    });

    var principal = SecurityContextUtils.getPrincipal();

    var newUserIds = prjUsersInfo.stream().map(UserProjectInfo::getId).toList();

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    expect(org.getId(), isEqual(project.getOrganizationId())).verify(ErrorType.PROJECT_NOT_FOUND, projectId);

    if (newUserIds.isEmpty()) {
      unassignAllUsersFromProject(project.getId(), orgId);
      return;
    } else {
      var usersToRemove = projectUserRepository.findAllByProject_IdAndUser_IdNotIn(project.getId(), newUserIds);
      var removeIds = usersToRemove.stream().map(pu -> pu.getUser().getId()).toList();
      projectUserRepository.deleteByProject_IdAndUser_IdIn(project.getId(), removeIds);
      log.info("Users {} have been removed from project with ID {}", removeIds, project.getId());
      usersToRemove.forEach(
          pu -> assignmentHelper.publishUserUnassignEvent(principal, pu.getUser(), orgId, project.getId()));
    }

    prjUsersInfo.forEach(info -> {
      var userId = Optional.ofNullable(info.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

      var user = userRepository.findById(userId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

      assignmentHelper.validateUserAssignment(org, principal, user);

      var orgUser = assignmentHelper.getOrganizationUser(org, principal, user);

      var projectRole = assignmentHelper.evaluateProjectRole(orgUser, info);

      var prjUser = projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), project.getId())
          .orElseGet(() -> new ProjectUser().withUser(user).withProject(project));
      prjUser.setProjectRole(projectRole);
      projectUserRepository.save(prjUser);

      assignmentHelper.publishUserAssignEvent(principal, user, orgId, projectId, projectRole);
    });
  }

  @Override
  public void remove(PatchOperation operation, Long orgId, Long projectId) {
    if (ObjectUtils.isEmpty(operation.getValue())) {
      unassignAllUsersFromProject(projectId, orgId);
      return;
    }
    var ids = readOperationValue(operation, new TypeReference<List<IdContainer>>() {
    });

    if (CollectionUtils.isEmpty(ids)) {
      unassignAllUsersFromProject(projectId, orgId);
      return;
    }

    var principal = SecurityContextUtils.getPrincipal();
    ids.forEach(idContainer -> {
      var projectUser = projectUserRepository.findProjectUserByUserIdAndProjectId(idContainer.getId(), projectId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, idContainer.getId()));
      projectUserRepository.deleteByUserIdAndProjectIds(idContainer.getId(), List.of(projectId));
      log.info("User with ID {} has been removed from project with ID {}", idContainer.getId(), projectId);
      assignmentHelper.publishUserUnassignEvent(principal, projectUser.getUser(), orgId, projectId);
    });
  }

  private void unassignAllUsersFromProject(Long projectId, Long orgId) {
    var principal = SecurityContextUtils.getPrincipal();
    var usersToRemove = projectUserRepository.findAllByProject_Id(projectId);
    projectUserRepository.deleteAllByProjectId(projectId);
    log.info("All users have been removed from project with ID {}", projectId);
    usersToRemove
        .forEach(pu -> assignmentHelper.publishUserUnassignEvent(principal, pu.getUser(), orgId, projectId));
  }
}
