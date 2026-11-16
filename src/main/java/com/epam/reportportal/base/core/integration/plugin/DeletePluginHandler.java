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

package com.epam.reportportal.base.core.integration.plugin;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.reporting.OperationCompletionRS;

/**
 * Removes a plugin from the runtime and database.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface DeletePluginHandler {

  /**
   * Delete plugin representation from the database and from the
   * {@link com.epam.reportportal.base.core.plugin.Pf4jPluginBox} instance
   *
   * @param id               {@link IntegrationType#id}
   * @param reportPortalUser {@link ReportPortalUser} that deleted plugin
   * @return {@link OperationCompletionRS} with result message
   */
  OperationCompletionRS deleteById(Long id, ReportPortalUser reportPortalUser);
}
