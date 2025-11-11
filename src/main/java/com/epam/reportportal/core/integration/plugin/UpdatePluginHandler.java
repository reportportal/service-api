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

package com.epam.reportportal.core.integration.plugin;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.model.integration.UpdatePluginStateRQ;
import com.epam.reportportal.reporting.OperationCompletionRS;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface UpdatePluginHandler {

  /**
   * Updates plugin state. If 'enabled == true', plugin file will be downloaded from the
   * {@link com.epam.reportportal.infrastructure.persistence.filesystem.DataStore} (if not exists in the plugins' root
   * path) and loaded in the memory. If 'enabled == false', plugin will be unloaded from the memory
   *
   * @param id                  {@link
   *                            com.epam.reportportal.infrastructure.persistence.entity.integration.IntegrationType#id}
   * @param updatePluginStateRQ {@link UpdatePluginStateRQ}
   * @param user                {@link ReportPortalUser} User that update plugin
   * @return {@link OperationCompletionRS}
   */
  OperationCompletionRS updatePluginState(Long id, UpdatePluginStateRQ updatePluginStateRQ,
      ReportPortalUser user);
}
