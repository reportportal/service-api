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

package com.epam.reportportal.extension.role;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.notNull;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.AbstractContextBasedCommand;
import java.util.Map.Entry;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract base class for plugin commands that operate within a project context.
 *
 * <p>Provides role validation logic for commands that require only a project context (no orgId
 * needed). Suitable for use with POST /api/plugins/{name}/commands/{commandName}.
 *
 * @param <T> the type of the command result
 */
public abstract class ProjectMemberContextCommand<T> extends AbstractContextBasedCommand<T> {

  protected final ProjectRepository projectRepository;
  protected final OrganizationRepositoryCustom organizationRepository;

  protected ProjectMemberContextCommand(ProjectRepository projectRepository,
      OrganizationRepositoryCustom organizationRepository) {
    this.projectRepository = projectRepository;
    this.organizationRepository = organizationRepository;
  }

  @Override
  public void validateRole(PluginCommandContext commandContext) {
    ReportPortalUser user =
        (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    expect(user, Objects::nonNull)
        .verify(ErrorType.ACCESS_DENIED);
    expect(commandContext, notNull())
        .verify(ErrorType.BAD_REQUEST_ERROR, "Context should not be null");
    expect(commandContext.getProjectId(), notNull())
        .verify(ErrorType.BAD_REQUEST_ERROR, "Project ID should not be null");

    Project project = projectRepository.findById(commandContext.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, commandContext.getProjectId()));

    validateProjectPermissions(user, project);
  }

  /**
   * Validates that the user has at minimum project-member access.
   *
   * <p>Subclasses may override this method to enforce a stricter role requirement (e.g. project
   * manager or editor).
   *
   * @param user    the authenticated user
   * @param project the resolved project
   */
  protected void validateProjectPermissions(ReportPortalUser user, Project project) {
    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }
    Organization organization = organizationRepository.findById(project.getOrganizationId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND));

    OrganizationRole orgRole = ofNullable(user.getOrganizationDetails())
        .flatMap(detailsMapping -> ofNullable(detailsMapping.get(organization.getName())))
        .map(ReportPortalUser.OrganizationDetails::getOrgRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

    if (orgRole.sameOrHigherThan(OrganizationRole.MANAGER)) {
      return;
    }

    user.getOrganizationDetails().entrySet().stream()
        .filter(entry -> entry.getKey().equals(organization.getName()))
        .map(Entry::getValue)
        .flatMap(orgDetails -> orgDetails.getProjectDetails().entrySet().stream())
        .map(Entry::getValue)
        .filter(details -> details.getProjectId().equals(project.getId()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
  }
}
