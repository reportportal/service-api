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

package com.epam.ta.reportportal.core.project.patch;

import com.epam.ta.reportportal.core.project.ProjectService;
import org.springframework.stereotype.Service;

/**
 * Handler for patching a project when no specific path is provided. Extends {@link BasePatchProjectHandler} to leverage
 * common patch logic.
 */
@Service
public class PatchProjectNoPathHandler extends BasePatchProjectHandler {

  /**
   * Constructs a new BasePatchProjectHandler with the specified project service.
   *
   * @param projectService service for project-related operations
   */
  protected PatchProjectNoPathHandler(ProjectService projectService) {
    super(projectService);
  }
}
