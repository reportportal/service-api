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

package com.epam.ta.reportportal.core.organization;

import com.epam.reportportal.api.model.CreateOrganizationRequest;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.UpdateOrganizationRequest;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import org.jclouds.rest.ResourceNotFoundException;

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
   * @return @{@link OrganizationInfo} containing the details of the created organization.
   */
  OrganizationInfo createOrganization(CreateOrganizationRequest createRequest);

  /**
   * Update an existing organization.
   *
   * @param organizationId   The ID of the organization to update.
   * @param updateRequest The new details for the organization.
   */
  void updateOrganization(Long organizationId, UpdateOrganizationRequest updateRequest);


  /**
   * Deletes an organization by ID.
   *
   * @param organizationId The ID of the organization to retrieve.
   * @throws ResourceNotFoundException If the organization with the specified ID does not exist.
   */
  void deleteOrganization(Long organizationId) throws ResourceNotFoundException;
}
