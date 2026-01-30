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

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.core.project.ProjectService;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.util.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handler for patch operations related to project users. Extends {@link BasePatchProjectHandler} to provide
 * user-specific patch logic.
 */
@Service
@Slf4j
public class PatchProjectUserHandler extends BasePatchProjectHandler {

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
  protected PatchProjectUserHandler(
      UserRepository userRepository,
      ProjectService projectService,
      ProjectUserRepository projectUserRepository,
      ObjectMapper objectMapper,
      OrganizationRepositoryCustom organizationRepository,
      ProjectRepository projectRepository,
      ProjectUserAssignmentHelper assignmentHelper
  ) {
    super(projectService, objectMapper);
    this.userRepository = userRepository;
    this.projectUserRepository = projectUserRepository;
    this.organizationRepository = organizationRepository;
    this.projectRepository = projectRepository;
    this.assignmentHelper = assignmentHelper;
  }

  @Override
  public void add(PatchOperation operation, Long orgId, Long projectId) {
    var userPrjInfo = readOperationValue(operation,
        new com.fasterxml.jackson.core.type.TypeReference<UserProjectInfo>() {
        });

    var principal = SecurityContextUtils.getPrincipal();

    var userId = Optional.ofNullable(userPrjInfo.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Field 'id' is required"));

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

    var org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    expect(org.getId(), isEqual(project.getOrganizationId())).verify(ErrorType.PROJECT_NOT_FOUND, projectId);

    assignmentHelper.validateUserAssignment(org, principal, user);

    projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), project.getId())
        .ifPresent(ignored -> {
          throw new ReportPortalException(ErrorType.UNABLE_ASSIGN_UNASSIGN_USER_TO_PROJECT, user.getId());
        });

    var orgUser = assignmentHelper.getOrganizationUser(org, principal, user);

    var projectRole = assignmentHelper.evaluateProjectRole(orgUser, userPrjInfo);

    var prjUser = new ProjectUser()
        .withUser(user)
        .withProject(project)
        .withProjectRole(projectRole);

    projectUserRepository.save(prjUser);

    assignmentHelper.publishUserAssignEvent(principal, user, orgId, projectId, projectRole);
  }
}
