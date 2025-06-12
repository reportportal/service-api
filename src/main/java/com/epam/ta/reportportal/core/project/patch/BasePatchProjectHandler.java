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

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.ta.reportportal.core.project.ProjectService;

/**
 * Abstract base class for handling patch operations on projects. Subclasses should override the adding, replace, and
 * remove methods to implement specific patch logic.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public abstract class BasePatchProjectHandler {

  protected final ProjectService projectService;

  /**
   * Constructs a new BasePatchProjectHandler with the specified project service.
   *
   * @param projectService service for project-related operations
   */
  protected BasePatchProjectHandler(ProjectService projectService) {
    this.projectService = projectService;
  }

  void replace(PatchOperation operation, Long orgId, Long projectId) {
    throw new UnsupportedOperationException("'Replace' operation is not supported");
  }

  void add(PatchOperation operation, Long orgId, Long projectId) {
    throw new UnsupportedOperationException("'Add' operation is not supported");
  }

  void remove(PatchOperation operation, Long orgId, Long projectId) {
    throw new UnsupportedOperationException("'Remove' operation is not supported");
  }

}
