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
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.project.ProjectService;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.model.project.ProjectUserRole;
import com.epam.ta.reportportal.util.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * Handler for patch operations related to project users. Extends {@link BasePatchProjectHandler} to provide
 * user-specific patch logic.
 */
@Service
public class PatchProjectUsersHandler extends BasePatchProjectHandler {

  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final ObjectMapper objectMapper;


  /**
   * Constructs a new PatchProjectUsersHandler.
   *
   * @param projectService        The project service to use.
   * @param projectUserRepository The repository for project users.
   * @param objectMapper          The object mapper for JSON conversion.
   */
  protected PatchProjectUsersHandler(ProjectService projectService, ProjectUserRepository projectUserRepository,
      OrganizationUserRepository organizationUserRepository,
      ObjectMapper objectMapper) {
    super(projectService);
    this.projectUserRepository = projectUserRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  @SneakyThrows
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    Set<ProjectUserRole> operationValues = objectMapper.readValue(
        String.valueOf(operation.getValue()),
        new com.fasterxml.jackson.core.type.TypeReference<>() {
        }
    );
    operationValues.forEach(pur -> replaceProjectUserRole(orgId, projectId, pur));
  }

  private void replaceProjectUserRole(Long orgId, Long projectId, ProjectUserRole pur) {
    expect(pur.id(), not(isEqual(SecurityContextUtils.getPrincipal().getUserId())))
        .verify(ErrorType.ACCESS_DENIED, "Self project role change is not allowed");

    var ou = organizationUserRepository.findByUserIdAndOrganization_Id(pur.id(), orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, pur.id()));
    var newRole = ou.getOrganizationRole().equals(OrganizationRole.MANAGER) ? ProjectRole.EDITOR : pur.role();

    projectUserRepository.findProjectUserByUserIdAndProjectId(pur.id(), projectId)
        .ifPresentOrElse(pru -> pru.setProjectRole(newRole), () -> {
          throw new ReportPortalException(ErrorType.USER_NOT_FOUND, pur.id());
        });
  }

}
