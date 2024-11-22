/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.admin;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.model.settings.ServerSettingsResource;
import com.epam.ta.reportportal.model.settings.UpdateSettingsRq;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Map;

/**
 * Server settings administration interface
 *
 * @author Andrei_Ramanchuk
 */
public interface ServerAdminHandler {

  /**
   * Get all server settings
   *
   * @return {@link ServerSettingsResource}
   */
  Map<String, String> getServerSettings();

  /**
   * Update analytics settings
   *
   * @param analyticsResource {@link AnalyticsResource}
   * @return Operation results
   */
  OperationCompletionRS saveAnalyticsSettings(AnalyticsResource analyticsResource);

  /**
   * Update server settings
   *
   * @param request {@link UpdateSettingsRq}
   * @return Operation results
   */
  OperationCompletionRS updateServerSettings(UpdateSettingsRq request, ReportPortalUser user);
}
