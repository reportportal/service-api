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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.ta.reportportal.core.project.ProjectService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handler for patch operations on projects within an organization. Supports patching project name and slug using
 * specific handlers. Ensures the project exists before applying patch operations.
 *
 * <p>This handler is configured as a Spring service component and uses logging capabilities:
 * <ul>
 *   <li>{@code @Service} - Indicates this class is a Spring service component for dependency
 *   injection
 *   <li>{@code @Slf4j} - Enables logging capabilities using Slf4j framework
 * </ul>
 *
 * <p>The handler processes patch operations through specialized handlers for different project
 * properties
 * and maintains transactional integrity during the patch operations.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 * @see org.springframework.stereotype.Service
 * @see lombok.extern.log4j.Log4j2
 */
@Service
@Slf4j
public class PatchProjectHandler {

  private final PatchProjectNameHandler patchProjectNameHandler;
  private final PatchProjectSlugHandler patchProjectSlugHandler;
  private final ProjectService projectService;

  /**
   * Constructs a new PatchProjectHandler with required dependencies.
   *
   * @param patchProjectNameHandler handler for project name patch operations
   * @param patchProjectSlugHandler handler for project slug patch operations
   * @param projectService          service for project-related operations
   */
  @Autowired
  public PatchProjectHandler(PatchProjectNameHandler patchProjectNameHandler,
      PatchProjectSlugHandler patchProjectSlugHandler,
      ProjectService projectService) {
    this.patchProjectNameHandler = patchProjectNameHandler;
    this.patchProjectSlugHandler = patchProjectSlugHandler;
    this.projectService = projectService;
  }

  /**
   * Applies a list of patch operations to a project within an organization. Verifies that the project exists within the
   * specified organization before applying any patches.
   *
   * @param patchOperations list of patch operations to be applied to the project
   * @param orgId           ID of the organization that owns the project
   * @param projectId       ID of the project to be patched
   * @throws com.epam.reportportal.rules.exception.ReportPortalException if a project is not found in the organization
   */
  public void patchOrganizationProject(List<PatchOperation> patchOperations, Long orgId,
      Long projectId) {
    expect(projectService.existsByProjectIdAndOrgId(projectId, orgId), equalTo(true))
        .verify(PROJECT_NOT_FOUND, projectId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      this.patchProject(operation, projectId);
    });
  }

  /**
   * Applies a single patch operation to a project. Determines the appropriate handler based on the operation path and
   * executes the corresponding patch operation (add, replace, or remove).
   *
   * @param operation the patch operation to be applied, containing the path, operation type and value
   * @param projectId ID of the project to be patched
   * @throws IllegalArgumentException if the operation path is invalid ("name" or "slug" expected) or if the operation
   *                                  type is not supported (ADD, REPLACE, or REMOVE expected)
   */
  public void patchProject(PatchOperation operation, Long projectId) {
    BasePatchProjectHandler patchOperationHandler = switch (operation.getPath()) {
      case "name" -> this.patchProjectNameHandler;
      case "slug" -> this.patchProjectSlugHandler;
      case null, default -> throw new IllegalArgumentException("Unexpected value: " + operation.getPath());
    };

    switch (operation.getOp()) {
      case ADD -> patchOperationHandler.add(operation, projectId);
      case REPLACE -> patchOperationHandler.replace(operation, projectId);
      case REMOVE -> patchOperationHandler.remove(operation, projectId);
      default -> throw new IllegalArgumentException("Unexpected value: " + operation.getOp());
    }

  }


}
