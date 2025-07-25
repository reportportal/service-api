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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * Handler for managing project name patch operations in the Report Portal system. Extends the base patch handler to
 * provide specific implementation for modifying project names through patch operations.
 *
 * <p>This handler processes patch operations related to project name updates,
 * ensuring proper validation and execution of the modifications.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 * @see BasePatchProjectHandler
 * @see ProjectService
 * @see PatchOperation
 */

@Service
public class PatchProjectNameHandler extends BasePatchProjectHandler {

  /**
   * Constructs a new PatchProjectNameHandler with the specified project service.
   *
   * @param projectService service for handling project-related operations
   */
  public PatchProjectNameHandler(ProjectService projectService, ObjectMapper objectMapper) {
    super(projectService, objectMapper);
  }

  /**
   * Replaces the project name with a new value from the patch operation.
   *
   * @param operation patch operation containing the new project name value
   * @param projectId identifier of the project to be updated
   */
  @Override
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    projectService.updateProjectName(orgId, projectId, (String) operation.getValue());
  }

}
