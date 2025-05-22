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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PatchProjectHandler {

  private final PatchProjectNameHandler patchProjectName;
  private final PatchProjectSlugHandler patchProjectSlug;
  private final ProjectService projectService;

  @Autowired
  public PatchProjectHandler(PatchProjectNameHandler patchProjectName, PatchProjectSlugHandler patchProjectSlug, ProjectService projectService) {
    this.patchProjectName = patchProjectName;
    this.patchProjectSlug = patchProjectSlug;
    this.projectService = projectService;
  }

  public void patchOrganizationProject(List<PatchOperation> patchOperations, Long orgId, Long projectId) {
    expect(projectService.existsByProjectIdAndOrgId(projectId, orgId), equalTo(true))
        .verify(PROJECT_NOT_FOUND, projectId);

    patchOperations.forEach(operation -> {
      log.debug("Patch operation: {}", operation);
      this.patchProject(operation, projectId);
    });
  }

  public void patchProject(PatchOperation operation, Long projectId) {
    BasePatchProjectHandler patchOperationHandler = switch (operation.getPath()) {
      case "name" -> this.patchProjectName;
      case "slug" -> this.patchProjectSlug;
      case null, default -> throw new IllegalStateException("Unexpected value: " + operation.getPath());
    };

    switch (operation.getOp()) {
      case ADD -> patchOperationHandler.add(operation, projectId);
      case REPLACE -> patchOperationHandler.replace(operation, projectId);
      case REMOVE -> patchOperationHandler.remove(operation, projectId);
    }

  }


}
