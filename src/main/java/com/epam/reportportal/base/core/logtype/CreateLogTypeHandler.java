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

package com.epam.reportportal.base.core.logtype;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;

/**
 * Interface for handling operations related to creating log types.
 */
public interface CreateLogTypeHandler {

  /**
   * Creates a new log type for the specified project.
   *
   * @param projectName The name of the project where the log type is to be created.
   * @param logType     The log type data containing details such as name, level, and visibility.
   * @param user        The user performing the action.
   * @return The newly created log type as a DTO.
   * @throws ReportPortalException if the project is not found or validation fails.
   */
  LogTypeResponse createLogType(String projectName, LogTypeRequest logType, ReportPortalUser user);
}
