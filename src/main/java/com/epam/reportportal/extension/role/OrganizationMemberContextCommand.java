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

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.AbstractContextBasedCommand;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract base class for organization context commands.
 * <p>
 * Provides common role validation logic for commands that require organization context.
 *
 * @param <T> the type of the command result
 */
public abstract class OrganizationMemberContextCommand<T> extends AbstractContextBasedCommand<T> {

  protected final OrganizationRepositoryCustom organizationRepository;
  protected final OrganizationUserRepository organizationUserRepository;

  protected OrganizationMemberContextCommand(OrganizationRepositoryCustom organizationRepository,
      OrganizationUserRepository organizationUserRepository) {
    this.organizationRepository = organizationRepository;
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public void validateRole(PluginCommandContext commandContext) {
    ReportPortalUser user =
        (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    expect(user, Objects::nonNull)
        .verify(ErrorType.ACCESS_DENIED);
    expect(commandContext, notNull())
        .verify(ErrorType.BAD_REQUEST_ERROR, "Context should not be null");
    expect(commandContext.getOrgId(), notNull())
        .verify(ErrorType.BAD_REQUEST_ERROR, "Organization ID should not be null");

    Organization organization = organizationRepository.findById(commandContext.getOrgId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, commandContext.getOrgId()));

    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }

    OrganizationUser organizationUser =
        organizationUserRepository.findByUserIdAndOrganization_Id(user.getUserId(), organization.getId())
            .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
                Suppliers.formattedSupplier("You are not assigned to the organization {}", commandContext.getOrgId())));

    // Allow subclasses to perform additional role validation
    validateOrganizationRole(organizationUser);
  }

  /**
   * Template method for subclasses to implement specific role validation logic. By default, this method does nothing
   * (membership validation is sufficient).
   *
   * @param organizationUser the organization user to validate
   */
  protected void validateOrganizationRole(OrganizationUser organizationUser) {
    // Default implementation - no additional validation required
  }
}
