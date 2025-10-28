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

import com.epam.ta.reportportal.commons.ReportPortalUser;

/**
 * Handler for deletion of log types from a project.
 */
public interface DeleteLogTypeHandler {

  /**
   * Deletes a log type by ID from the specified project.
   *
   * @param projectName The name of the project.
   * @param logTypeId   The ID of the log type to delete.
   * @param user        The user performing the action.
   */
  void deleteLogType(String projectName, Long logTypeId, ReportPortalUser user);
}
