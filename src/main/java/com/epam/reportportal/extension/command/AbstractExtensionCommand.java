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

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for plugin extension commands that require role-based access control.
 *
 * <p>Subclasses declare the minimum required {@link #minUserRole}, {@link #minOrgRole}, and/or
 * {@link #minProjectRole}, then implement either {@link #invokeCommand(PluginCommandRQ)} or
 * {@link #invokeCommand(Integration, PluginCommandRQ)} (or both). Role validation runs automatically before the command
 * is invoked.
 *
 * <p>The project-level check derives the organization from the project record itself, so a caller
 * cannot bypass the check by supplying a mismatched {@code orgId}.
 *
 * @param <T> the return type of the command
 */
@Slf4j
public abstract class AbstractExtensionCommand<T> implements ExtensionCommand<T> {

  protected ProjectRole minProjectRole;
  protected OrganizationRole minOrgRole;
  protected UserRole minUserRole;

  private final ProjectRepository projectRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationRepository organizationRepository;
  private final ProjectUserRepository projectUserRepository;

  protected AbstractExtensionCommand(
      ProjectRepository projectRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository,
      ProjectUserRepository projectUserRepository) {
    this.projectRepository = projectRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.organizationRepository = organizationRepository;
    this.projectUserRepository = projectUserRepository;
  }


  /**
   * Override to handle a command that operates on a specific {@link Integration}.
   *
   * @param integration     the integration to act on
   * @param pluginCommandRq the incoming command request
   * @return the command result
   * @throws UnsupportedOperationException if the subclass does not support this variant
   */
  protected T invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution with an integration");
  }

  /**
   * Override to handle a command that does not require an {@link Integration}.
   *
   * @param pluginCommandRq the incoming command request
   * @return the command result
   * @throws UnsupportedOperationException if the subclass does not support this variant
   */
  protected T invokeCommand(PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution without an integration");
  }

  @Override
  public T executeCommand(PluginCommandRQ pluginCommandRq) {
    var context = pluginCommandRq.getContext();
    var orgId = context != null ? context.getOrgId() : null;
    var projectId = context != null ? context.getProjectId() : null;
    validateRole(orgId, projectId);
    return invokeCommand(pluginCommandRq);
  }

  @Override
  public T executeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    validateIntegrationRole(integration);
    return invokeCommand(integration, pluginCommandRq);
  }

  /**
   * Validates that the current user holds the required role.
   *
   * <p>Checks are evaluated in order: project role (when {@link #minProjectRole} and a
   * {@code projectId} are present), organization role (when {@link #minOrgRole} and an {@code orgId} are present), or
   * plain user role otherwise. Administrators bypass all checks. When {@link #minUserRole} is {@code null} the method
   * returns immediately.
   *
   * @param orgId     organization ID from the request context, may be {@code null}
   * @param projectId project ID from the request context, may be {@code null}
   * @throws ReportPortalException with {@code ACCESS_DENIED} if the user lacks the required role
   */
  protected void validateRole(Long orgId, Long projectId) {
    if (minUserRole == null) {
      return;
    }

    ReportPortalUser user = SecurityContextUtils.getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }

    if (minProjectRole != null) {
      if (projectId == null) {
        throw new ReportPortalException(ErrorType.ACCESS_DENIED);
      }
      validateProjectRole(user, projectId);
    } else if (minOrgRole != null) {
      if (orgId == null) {
        throw new ReportPortalException(ErrorType.ACCESS_DENIED);
      }
      validateOrgRole(user, orgId);
    } else if (minUserRole != user.getUserRole()) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED);
    }
  }

  protected void validateIntegrationRole(Integration integration) {
    var orgId = integration.getOrganizationId();
    var projectId = integration.getProject() != null ? integration.getProject().getId() : null;
    if (minUserRole == null) {
      return;
    }

    ReportPortalUser user = SecurityContextUtils.getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }

    if (minProjectRole != null && projectId != null) {
      validateProjectRole(user, projectId);
    } else if (minOrgRole != null && projectId != null) {
      validateOrgRole(user, orgId);
    } else if (minUserRole != user.getUserRole()) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED);
    }
  }

  private void validateProjectRole(ReportPortalUser user, Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
    Long projectOrgId = project.getOrganizationId();
    OrganizationRole orgRole = findOrgRole(user.getUserId(), projectOrgId);

    if (orgRole.sameOrHigherThan(OrganizationRole.MANAGER)) {
      return;
    }
    ProjectRole projectRole = projectUserRepository.findProjectUserByUserIdAndProjectId(user.getUserId(), projectId)
        .map(ProjectUser::getProjectRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
    BusinessRule.expect(projectRole, minProjectRole::sameOrLowerThan)
        .verify(ErrorType.ACCESS_DENIED);
  }

  private void validateOrgRole(ReportPortalUser user, Long orgId) {
    OrganizationRole orgRole = findOrgRole(user.getUserId(), orgId);
    BusinessRule.expect(orgRole, role -> role.sameOrHigherThan(minOrgRole))
        .verify(ErrorType.ACCESS_DENIED);
  }

  private OrganizationRole findOrgRole(Long userId, Long orgId) {
    return organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId)
        .map(OrganizationUser::getOrganizationRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
  }

}
