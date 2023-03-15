/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.project.config;

import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ProjectConfigProvider {

  private final GetProjectHandler getProjectHandler;

  @Autowired
  public ProjectConfigProvider(GetProjectHandler getProjectHandler) {
    this.getProjectHandler = getProjectHandler;
  }

  @Transactional(readOnly = true)
  public Map<String, String> provide(Long projectId) {
    final Project project = getProjectHandler.get(projectId);
    return ProjectUtils.getConfigParameters(project.getProjectAttributes());
  }
}
