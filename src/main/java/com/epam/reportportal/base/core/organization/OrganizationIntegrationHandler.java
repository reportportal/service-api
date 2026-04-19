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

package com.epam.reportportal.base.core.organization;

import com.epam.reportportal.api.model.OrganizationIntegration;
import com.epam.reportportal.api.model.OrganizationIntegrationPage;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import org.springframework.data.domain.Pageable;

/**
 * Handler interface for managing organization-scoped integrations.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public interface OrganizationIntegrationHandler {

  /**
   * Retrieve integrations belonging to the given organization with pagination.
   *
   * @param orgId    Organization ID
   * @param pageable Pagination and sort parameters
   * @return {@link OrganizationIntegrationPage}
   */
  OrganizationIntegrationPage getOrganizationIntegrations(Long orgId, Pageable pageable);

  /**
   * Retrieve a specific integration by ID, scoped to the given organization.
   *
   * @param orgId         Organization ID
   * @param integrationId Integration ID
   * @return {@link OrganizationIntegration}
   */
  OrganizationIntegration getOrganizationIntegrationById(Long orgId, Long integrationId);

  /**
   * Create a new integration scoped to the given organization.
   *
   * @param orgId         Organization ID
   * @param pluginName    Plugin name
   * @param createRequest {@link IntegrationRQ}
   * @return {@link EntryCreatedRS}
   */
  EntryCreatedRS createOrganizationIntegration(Long orgId, String pluginName,
      IntegrationRQ createRequest);

  /**
   * Update an existing integration scoped to the given organization.
   *
   * @param orgId         Organization ID
   * @param integrationId Integration ID
   * @param updateRequest {@link IntegrationRQ}
   */
  void updateOrganizationIntegration(Long orgId, Long integrationId, IntegrationRQ updateRequest);

  /**
   * Delete a specific integration scoped to the given organization.
   *
   * @param orgId         Organization ID
   * @param integrationId Integration ID
   */
  void deleteOrganizationIntegration(Long orgId, Long integrationId);

  /**
   * Delete all integrations of the given plugin type within the organization.
   *
   * @param orgId      Organization ID
   * @param pluginName Plugin name
   */
  void deleteOrganizationIntegrationsByType(Long orgId, String pluginName);
}
