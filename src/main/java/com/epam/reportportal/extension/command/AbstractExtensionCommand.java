/*
 * Copyright (C) 2026 EPAM Systems
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

package com.epam.reportportal.extension.command;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExtensionCommand<T> implements ExtensionCommand<T> {

  protected ProjectRole minProjectRole;
  protected OrganizationRole minOrgRole;
  protected UserRole minUserRole;


  protected final ProjectRepository projectRepository;
  protected final OrganizationRepositoryCustom organizationRepository;

  protected AbstractExtensionCommand(ProjectRepository projectRepository,
      OrganizationRepositoryCustom organizationRepository) {
    this.projectRepository = projectRepository;
    this.organizationRepository = organizationRepository;
  }

  protected T invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution with an integration");
  }

  protected T invokeCommand(PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution without an integration");
  }

  @Override
  public T executeCommand(PluginCommandRQ pluginCommandRq) {
    validateRole(pluginCommandRq.getContext());
    return invokeCommand(pluginCommandRq);
  }


  @Override
  public T executeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    validateRole(pluginCommandRq.getContext());
    return invokeCommand(integration, pluginCommandRq);
  }

  protected void validateRole(PluginCommandContext commandContext) {
    ReportPortalUser user = SecurityContextUtils.getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED); // TODO: could block public commands

    if (minUserRole != null) {
      if (user.getUserRole() == UserRole.ADMINISTRATOR) {
        return;
      }

      if (minProjectRole != null && commandContext.getProjectId() != null) {
        validateProjectRole(user, commandContext);
      } else if (minOrgRole != null && commandContext.getOrgId() != null) {
        validateOrgRole(user, commandContext);
      } else if (minUserRole != user.getUserRole()) {
        validateOrgRole(user, commandContext);
      }
    }
  }

  private void validateProjectRole(ReportPortalUser user, PluginCommandContext commandContext) {
    Project project = ofNullable(commandContext.getOrgId())
        .map(orgId -> projectRepository.findByIdAndOrganizationId(commandContext.getProjectId(), orgId))
        .orElseGet(() -> projectRepository.findById(commandContext.getProjectId()))
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, commandContext.getProjectId()));

    Organization organization = organizationRepository.findById(project.getOrganizationId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND));

    OrganizationRole orgRole = ofNullable(user.getOrganizationDetails())
        .flatMap(detailsMapping -> ofNullable(detailsMapping.get(organization.getName())))
        .map(ReportPortalUser.OrganizationDetails::getOrgRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

    if (orgRole.sameOrHigherThan(OrganizationRole.MANAGER)) {
      return;
    }

    ProjectRole projectRole = user.getOrganizationDetails().entrySet().stream()
        .filter(entry -> entry.getKey().equals(organization.getName()))
        .map(Entry::getValue)
        .flatMap(orgDetails -> orgDetails.getProjectDetails().entrySet().stream())
        .map(Entry::getValue)
        .filter(details -> details.getProjectId().equals(project.getId()))
        .map(ReportPortalUser.OrganizationDetails.ProjectDetails::getProjectRole)
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

    BusinessRule.expect(projectRole, minProjectRole::sameOrLowerThan)
        .verify(ErrorType.ACCESS_DENIED);
  }

  private void validateOrgRole(ReportPortalUser user, PluginCommandContext commandContext) {
    Organization organization = organizationRepository.findById(commandContext.getOrgId())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, commandContext.getOrgId()));

    OrganizationRole orgRole = ofNullable(user.getOrganizationDetails())
        .flatMap(detailsMapping -> ofNullable(detailsMapping.get(organization.getName())))
        .map(ReportPortalUser.OrganizationDetails::getOrgRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

    BusinessRule.expect(orgRole, role -> role.sameOrHigherThan(minOrgRole))
        .verify(ErrorType.ACCESS_DENIED);
  }


}
