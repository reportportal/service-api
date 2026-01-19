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

package com.epam.reportportal.core.logtype.impl;

import com.epam.reportportal.api.model.GetLogTypes200Response;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.core.logtype.GetLogTypeHandler;
import com.epam.reportportal.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.ws.converter.converters.LogTypeConverter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link GetLogTypeHandler} to retrieve log types for a project.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetLogTypeHandlerImpl implements GetLogTypeHandler {

  private final ProjectRepository projectRepository;
  private final LogTypeRepository logTypeRepository;

  /**
   * Retrieves log types for the specified project.
   *
   * @param projectKey The key of the project.
   * @return A response containing the log types for the project.
   * @throws ReportPortalException If the project is not found.
   */
  @Override
  public GetLogTypes200Response getLogTypes(String projectKey) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    List<LogTypeResponse> items = logTypeRepository.findByProjectId(project.getId()).stream()
        .map(LogTypeConverter.TO_RESOURCE)
        .toList();

    return new GetLogTypes200Response(items);
  }
}
