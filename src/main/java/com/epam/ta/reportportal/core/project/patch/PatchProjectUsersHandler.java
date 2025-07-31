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

package com.epam.ta.reportportal.core.project.patch;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.project.ProjectService;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.model.IdContainer;
import com.epam.ta.reportportal.util.SecurityContextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;

  /**
   * Constructs a new PatchProjectUsersHandler.
   *
   * @param projectService        The project service to use.
   * @param projectUserRepository The repository for project users.
   * @param objectMapper          The object mapper for JSON conversion.
   */
  @Autowired
  protected PatchProjectUsersHandler(ProjectService projectService, ProjectUserRepository projectUserRepository,
      OrganizationUserRepository organizationUserRepository,
      ObjectMapper objectMapper) {
    super(projectService, objectMapper);
    this.projectUserRepository = projectUserRepository;
    this.organizationUserRepository = organizationUserRepository;
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

}
