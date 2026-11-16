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

package com.epam.reportportal.base.core.integration;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;

/**
 * Handler for creating or updating project and global integrations.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreateIntegrationHandler {

  /**
   * Create {@link Integration} with {@link Integration#project == NULL}
   *
   * @param pluginName    Plugin name
   * @param createRequest {@link IntegrationRQ}
   * @param user          {@link ReportPortalUser}
   * @return {@link EntryCreatedRS}
   */
  EntryCreatedRS createGlobalIntegration(IntegrationRQ createRequest, String pluginName,
      ReportPortalUser user);

  /**
   * Create {@link Integration} for {@link Project} with provided ID
   *
   * @param projectName   Project name
   * @param createRequest {@link IntegrationRQ}
   * @param pluginName    Plugin name
   * @param user          {@link ReportPortalUser}
   * @return {@link EntryCreatedRS}
   */
  EntryCreatedRS createProjectIntegration(String projectName, IntegrationRQ createRequest,
      String pluginName, ReportPortalUser user);

  /**
   * Update {@link Integration} with {@link Integration#project == NULL}
   *
   * @param id            {@link Integration#id}
   * @param updateRequest {@link IntegrationRQ}
   * @param user          {@link ReportPortalUser}
   * @return updated {@link Integration}
   */
  OperationCompletionRS updateGlobalIntegration(Long id, IntegrationRQ updateRequest,
      ReportPortalUser user);

  /**
   * Updated {@link Integration} for {@link Project} with provided ID
   *
   * @param id            {@link Integration#id}
   * @param projectName   Project name
   * @param updateRequest {@link IntegrationRQ}
   * @param user          {@link ReportPortalUser}
   * @return updated {@link Integration}
   */
  OperationCompletionRS updateProjectIntegration(Long id, String projectName,
      IntegrationRQ updateRequest, ReportPortalUser user);
}
