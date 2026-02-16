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

package com.epam.reportportal.base.core.organization.patch;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.notNull;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.base.core.organization.GetOrganizationHandler;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handler for patch operations on organizations within an organization.
 * Supports patching organization name and slug using specific handlers.
 * Ensures the organization exists before applying patch operations.
 *
 * <p>This handler is configured as a Spring service component and uses logging capabilities:
 * <ul>
 *   <li>{@code @Service} - Indicates this class is a Spring service component for dependency
 *   injection
 *   <li>{@code @Slf4j} - Enables logging capabilities using Slf4j framework
 * </ul>
 *
 * <p>The handler processes patch operations through specialized handlers
 * for different organization properties
 * and maintains transactional integrity during the patch operations.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 * @see Service
 * @see lombok.extern.log4j.Log4j2
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatchOrganizationHandler {
  private final GetOrganizationHandler organizationHandler;
  private final PatchOrganizationUsersHandler patchOrganizationUsersHandler;
  private final PatchOrganizationUserAddHandler patchOrganizationAddUserHandler;

  /**
   * Applies a list of patch operations to an organization.
   * Verifies that the organization exists before applying any patches.
   *
   * @param patchOperations list of patch operations to be applied to the organization
   * @param orgId           ID of the organization that owns the organization
   * @throws ReportPortalException if an organization is not found
   */
  public void patchOrganization(List<PatchOperation> patchOperations, Long orgId) {
    expect(organizationHandler.getOrganizationById(orgId), notNull())
        .verify(NOT_FOUND, "Organization " + orgId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      this.patchOrganization(operation, orgId);
    });
  }

  /**
   * Applies a single patch operation to a organization.
   * Determines the appropriate handler based on the operation path and
   * executes the corresponding patch operation (add, replace, or remove).
   *
   * @param operation the patch operation to be applied, containing the path, operation type and value
   * @param orgId     ID of the organization
   * @throws IllegalArgumentException if the operation path is invalid ("name" or "slug" expected) or if the operation
   *                                  type is not supported (ADD, REPLACE, or REMOVE expected)
   */
  public void patchOrganization(PatchOperation operation, Long orgId) {
    BasePatchOrganizationHandler patchOperationHandler = switch (operation.getPath()) {
      case "/users" -> this.patchOrganizationUsersHandler;
      case "/users/-" -> this.patchOrganizationAddUserHandler;
      case null -> throw new IllegalArgumentException("Field 'path' is required");
      default -> throw new IllegalArgumentException("Unexpected path: '%s'".formatted(operation.getPath()));
    };

    switch (operation.getOp()) {
      case ADD -> patchOperationHandler.add(operation, orgId);
      case REPLACE -> patchOperationHandler.replace(operation, orgId);
      case REMOVE -> patchOperationHandler.remove(operation, orgId);
      default -> throw new IllegalArgumentException("Unexpected operation: '%s'".formatted(operation.getOp()));
    }
  }
}
