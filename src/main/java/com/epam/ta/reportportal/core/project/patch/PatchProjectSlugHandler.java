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
import org.springframework.stereotype.Service;

@Service
public class PatchProjectSlugHandler extends BasePatchProjectHandler {

  public PatchProjectSlugHandler(ProjectService projectService) {
    super(projectService);
  }

  @Override
  public void replace(PatchOperation operation, Long projectId) {
    var slug = SlugUtils.slug((String) operation.getValue());
    projectService.updateProjectSlug(projectId, slug);
  }

}
