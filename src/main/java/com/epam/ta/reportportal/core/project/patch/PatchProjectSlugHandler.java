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
import com.epam.ta.reportportal.util.SlugUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Handler for updating the project slugs through patch operations. This handler specifically manages the modification
 * of project-URL-friendly identifiers (slugs).
 *
 * <p>This handler extends the base patch project handler and is configured as a Spring service
 * component to handle slug-specific patch operations on projects:
 * <ul>
 *   <li>Validates and processes slug modifications
 *   <li>Ensures slugs are URL-friendly using SlugUtils
 *   <li>Updates project slugs in the database
 * </ul>
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 * @see BasePatchProjectHandler
 * @see SlugUtils
 */
@Service
public class PatchProjectSlugHandler extends BasePatchProjectHandler {

  /**
   * Constructs a new PatchProjectSlugHandler with the required project service.
   *
   * @param projectService service for project-related operations. Must not be null
   */
  public PatchProjectSlugHandler(ProjectService projectService, ObjectMapper objectMapper) {
    super(projectService, objectMapper);
  }

  /**
   * Replaces the current project slug with a new value. The operation value is converted to a valid URL-friendly slug
   * format before updating the project.
   *
   * <p>This method performs the following steps:
   * <ul>
   *   <li>Extracts the new slug value from the patch operation
   *   <li>Converts the value to a valid slug format using SlugUtils
   *   <li>Updates the project's slug in the database
   * </ul>
   *
   * @param operation the patch operation containing the new slug value
   * @param projectId the ID of the project to be updated
   * @throws ClassCastException if the operation value cannot be cast to String
   * @see SlugUtils#slug(String)
   */
  @Override
  public void replace(PatchOperation operation, Long orgId, Long projectId) {
    var slug = SlugUtils.slug((String) operation.getValue());
    if (StringUtils.isEmpty(slug)) {
      projectService.regenerateProjectSlug(projectId);
    } else {
      projectService.updateProjectSlug(orgId, projectId, slug);
    }
  }

}
