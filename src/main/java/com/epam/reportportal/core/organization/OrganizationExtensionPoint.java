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

package com.epam.reportportal.core.organization;

import com.epam.reportportal.api.model.CreateOrganizationRequest;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.UpdateOrganizationRequest;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;

/**
 * Extension point for organization management in ReportPortal.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public interface OrganizationExtensionPoint extends ReportPortalExtensionPoint {

  /**
   * Creates a new organization based on the provided request.
   *
   * @param createRequest The request containing the details of the organization to be created.
   * @param principal     The user performing the creation operation.
   * @return @{@link OrganizationInfo} containing the details of the created organization.
   */
  OrganizationInfo createOrganization(CreateOrganizationRequest createRequest, ReportPortalUser principal);

  /**
   * Creates a personal organization for the specified user.
   *
   * @param userId The ID of the user for whom to create the personal organization.
   * @return @{@link OrganizationInfo} containing the details of the created personal organization.
   */
  OrganizationInfo createPersonalOrganization(long userId);

  /**
   * Update an existing organization.
   *
   * @param organizationId The ID of the organization to update.
   * @param updateRequest  The new details for the organization.
   * @param principal      The user performing the update operation.
   * @throws ReportPortalException If the organization with the specified ID does not exist.
   */
  void updateOrganization(Long organizationId, UpdateOrganizationRequest updateRequest, ReportPortalUser principal)
      throws ReportPortalException;


  /**
   * Deletes an organization by ID.
   *
   * @param organizationId The ID of the organization to retrieve.
   * @param principal      The user performing the delete operation.
   * @throws ReportPortalException If the organization with the specified ID does not exist.
   */
  void deleteOrganization(Long organizationId, ReportPortalUser principal) throws ReportPortalException;
}
