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

package com.epam.reportportal.base.core.project.settings.impl;

import com.epam.reportportal.base.core.project.settings.GetProjectSettingsHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.project.config.ProjectSettingsResource;
import com.epam.reportportal.base.ws.converter.converters.ProjectSettingsConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns general project settings for the UI.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional(readOnly = true)
public class GetProjectSettingsHandlerImpl implements GetProjectSettingsHandler {

  private final ProjectRepository projectRepository;

  @Autowired
  public GetProjectSettingsHandlerImpl(ProjectRepository repository) {
    this.projectRepository = repository;
  }

  @Override
  public ProjectSettingsResource getProjectSettings(String projectKey) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));
    return ProjectSettingsConverter.TO_PROJECT_SETTINGS_RESOURCE.apply(project);
  }
}
