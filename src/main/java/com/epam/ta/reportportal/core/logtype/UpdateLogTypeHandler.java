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

package com.epam.ta.reportportal.core.logtype;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;

/**
 * Handler for updating log types in a project.
 */
public interface UpdateLogTypeHandler {

  /**
   * Updates a log type by ID in the specified project.
   *
   * @param projectName The name of the project.
   * @param logTypeId   The ID of the log type to update.
   * @param logType     The log type data containing the updated fields.
   * @param user        The user performing the action.
   * @return SuccessfulUpdate
   * @throws ReportPortalException if the project or log type is not found, or validation fails.
   */
  SuccessfulUpdate updateLogType(String projectName, Long logTypeId, LogTypeRequest logType,
      ReportPortalUser user);
}

