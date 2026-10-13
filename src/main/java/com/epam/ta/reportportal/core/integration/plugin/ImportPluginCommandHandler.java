/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles plugin import command execution.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ImportPluginCommandHandler {

  /**
   * Executes import command for a plugin instance.
   *
   * @param user           report portal user
   * @param projectName    project name
   * @param pluginName     plugin name
   * @param file           import file
   * @param launchImportRq import request
   * @return plugin command execution result
   */
  Object execute(ReportPortalUser user, String projectName, String pluginName, MultipartFile file,
      LaunchImportRQ launchImportRq);
}
