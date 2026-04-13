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
import com.epam.reportportal.base.core.events.domain.ProjectUsersUpdatedEvent;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
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
 * Handler for patch operations related to project users. Supports bulk assign/unassign operations with corresponding
 * bulk events.
 */
@Service
public class PatchProjectUsersHandler extends BasePatchProjectHandler {

  private final UserRepository userRepository;
  private final OrganizationRepositoryCustom organizationRepository;
  private final ProjectRepository projectRepository;
  private final ProjectUserRepository projectUserRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ProjectUserAssignmentHelper assignmentHelper;

  /**
   * Constructs a new PatchProjectUsersHandler.
   *
   * @param projectService        The project service to use.
   * @param projectUserRepository The repository for project users.
   * @param objectMapper          The object mapper for JSON conversion.
   */
  protected PatchProjectUsersHandler(
      UserRepository userRepository,
      ProjectService projectService,
      ProjectUserRepository projectUserRepository,
      ObjectMapper objectMapper,
      OrganizationRepositoryCustom organizationRepository,
      ProjectRepository projectRepository,
      ApplicationEventPublisher eventPublisher,
      ProjectUserAssignmentHelper assignmentHelper) {
    super(projectService, objectMapper);
    this.userRepository = userRepository;
    this.projectUserRepository = projectUserRepository;
    this.organizationRepository = organizationRepository;
    this.projectRepository = projectRepository;
    this.eventPublisher = eventPublisher;
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
    var principal = getPrincipal();

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
    var project = findAndValidateProject(orgId, projectId);

    var beforeUserIds = projectUserRepository.findUserIdsByProjectId(projectId);

    if (CollectionUtils.isEmpty(prjUsersInfo)) {
      projectUserRepository.deleteAllByProjectId(projectId);
      publishEvent(principal, project, orgId, beforeUserIds, List.of(), EventAction.UNASSIGN);
      return;
    }

    var newUserIds = prjUsersInfo.stream()
        .map(UserProjectInfo::getId)
        .filter(Objects::nonNull)
        .toList();

    // Remove users not in new list
    var removedUserIds = projectUserRepository.deleteByProjectIdAndUserIdNotInReturningUserIds(projectId, newUserIds);
    var afterRemovalUserIds = beforeUserIds.stream()
        .filter(id -> !removedUserIds.contains(id))
        .toList();
    publishEvent(principal, project, orgId, beforeUserIds, afterRemovalUserIds, EventAction.UNASSIGN);

    // Process assignments
    var assignedUserIds = processUserAssignments(prjUsersInfo, org, project, principal);
    if (!assignedUserIds.isEmpty()) {
      var afterAssignUserIds = new ArrayList<>(afterRemovalUserIds);
      afterAssignUserIds.addAll(assignedUserIds);
      publishEvent(principal, project, orgId, afterRemovalUserIds, afterAssignUserIds, EventAction.ASSIGN);
    }
  }

  @Override
  public void remove(PatchOperation operation, Long orgId, Long projectId) {
    var principal = getPrincipal();
    var project = findAndValidateProject(orgId, projectId);

    var beforeUserIds = projectUserRepository.findUserIdsByProjectId(projectId);
    var userIdsToRemove = extractUserIdsToRemove(operation);

    if (CollectionUtils.isEmpty(userIdsToRemove)) {
      projectUserRepository.deleteAllByProjectId(projectId);
      publishEvent(principal, project, orgId, beforeUserIds, List.of(), EventAction.UNASSIGN);
      return;
    }

    validateUsersExistInProject(userIdsToRemove, projectId);
    projectUserRepository.deleteByProjectIdAndUserIds(projectId, userIdsToRemove);

    var afterUserIds = beforeUserIds.stream()
        .filter(id -> !userIdsToRemove.contains(id))
        .toList();

    publishEvent(principal, project, orgId, beforeUserIds, afterUserIds, EventAction.UNASSIGN);
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private Project findAndValidateProject(Long orgId, Long projectId) {
    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
    expect(project.getOrganizationId(), isEqual(orgId)).verify(ErrorType.PROJECT_NOT_FOUND, projectId);
    return project;
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

  private List<Long> processUserAssignments(List<UserProjectInfo> prjUsersInfo, Organization org, Project project,
      ReportPortalUser principal) {

    var userIds = prjUsersInfo.stream()
        .map(info -> Optional.ofNullable(info.getId())
            .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required")))
        .toList();
    if (CollectionUtils.isEmpty(userIds)) {
      return List.of();
    }

    var usersById = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

    var existingProjectUsersById = projectUserRepository
        .findAllByProjectIdAndUserIdIn(project.getId(), userIds)
        .stream()
        .collect(Collectors.toMap(pu -> pu.getUser().getId(), Function.identity()));

    var projectUsersToSave = new ArrayList<ProjectUser>();
    var newlyAssignedUserIds = new ArrayList<Long>();

    for (var info : prjUsersInfo) {
      var user = Optional.ofNullable(usersById.get(info.getId()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, info.getId()));

      assignmentHelper.validateUserAssignment(org, principal, user);
      var orgUser = assignmentHelper.getOrganizationUser(org, principal, user);
      var projectRole = assignmentHelper.evaluateProjectRole(orgUser, info);

      var existingProjectUser = existingProjectUsersById.get(user.getId());
      if (existingProjectUser == null) {
        projectUsersToSave.add(new ProjectUser()
            .withUser(user)
            .withProject(project)
            .withProjectRole(projectRole));
        newlyAssignedUserIds.add(user.getId());
      } else {
        existingProjectUser.withProjectRole(projectRole);
        projectUsersToSave.add(existingProjectUser);
      }
    }

    if (!projectUsersToSave.isEmpty()) {
      projectUserRepository.saveAll(projectUsersToSave);
    }

    return newlyAssignedUserIds;
  }

  private void validateUsersExistInProject(List<Long> userIds, Long projectId) {
    var existingUserIds = projectUserRepository.findUserIdsByProjectIdAndUserIdIn(projectId, userIds);
    var missingUserIds = userIds.stream()
        .filter(id -> !existingUserIds.contains(id))
        .toList();

    if (!missingUserIds.isEmpty()) {
      throw new ReportPortalException(ErrorType.USER_NOT_FOUND, missingUserIds.getFirst());
    }
  }

  private void publishEvent(ReportPortalUser principal, Project project, Long orgId, List<Long> beforeUserIds,
      List<Long> afterUserIds, EventAction action) {
    if (beforeUserIds.equals(afterUserIds)) {
      return;
    }
    eventPublisher.publishEvent(
        new ProjectUsersUpdatedEvent(principal.getUserId(), principal.getUsername(), project.getId(), project.getName(),
            orgId, beforeUserIds, afterUserIds, action));
  }
}
