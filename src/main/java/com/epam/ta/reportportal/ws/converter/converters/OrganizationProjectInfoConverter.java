/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.OrganizationProjectInfo;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.function.Function;

/**
 * Converts project entity into OrganizationProjectInfo api model.
 *
 * @author Siarhei Hrabko
 */
public final class OrganizationProjectInfoConverter {

  private OrganizationProjectInfoConverter() {
    //static only
  }

  public static Function<Project, OrganizationProjectInfo> TO_ORG_PROJECT_INFO = project -> {
    if (project == null) {
      return null;
    }
    OrganizationProjectInfo projectInfo = new OrganizationProjectInfo();

    projectInfo.setId(project.getId());
    projectInfo.setName(project.getName());
    projectInfo.setSlug(project.getSlug());
    projectInfo.setKey(project.getKey());
    projectInfo.setOrganizationId(project.getOrganizationId());
    projectInfo.setCreatedAt(project.getCreationDate());
    projectInfo.setUpdatedAt(project.getUpdatedAt());
    return projectInfo;
  };

}
