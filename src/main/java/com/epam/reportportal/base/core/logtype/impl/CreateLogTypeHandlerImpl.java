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

package com.epam.reportportal.base.core.logtype.impl;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.base.core.events.domain.LogTypeCreatedEvent;
import com.epam.reportportal.base.core.logtype.CreateLogTypeHandler;
import com.epam.reportportal.base.core.logtype.validator.LogTypeValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.converter.builders.LogTypeBuilder;
import com.epam.reportportal.base.ws.converter.converters.LogTypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateLogTypeHandlerImpl implements CreateLogTypeHandler {

  private final ProjectRepository projectRepository;
  private final LogTypeRepository logTypeRepository;
  private final LogTypeValidator logTypeValidator;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates a new log type for a specific project after performing necessary validations.
   *
   * @param projectKey The key of the project where the log type should be created.
   * @param logType    The log type payload containing the details of the log type to be created.
   * @param user       The user performing the action.
   * @return The newly created log type as a DTO.
   * @throws ReportPortalException if the project is not found, validation fails, or the log type cannot be created.
   */
  @Override
  @Transactional
  public LogTypeResponse createLogType(String projectKey, LogTypeRequest logType,
      ReportPortalUser user) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));
    Long projectId = project.getId();

    validateLogType(projectId, logType);

    ProjectLogType projectLogType = new LogTypeBuilder()
        .addProjectId(projectId)
        .addName(logType.getName())
        .addLevel(logType.getLevel())
        .addStyle(logType.getStyle())
        .addIsFilterable(logType.getIsFilterable())
        .get();

    ProjectLogType savedEntity = logTypeRepository.save(projectLogType);

    eventPublisher.publishEvent(
        new LogTypeCreatedEvent(LogTypeConverter.TO_ACTIVITY_RESOURCE.apply(savedEntity),
            user.getUserId(), user.getUsername()
        ));

    return LogTypeConverter.TO_RESOURCE.apply(savedEntity);
  }

  private void validateLogType(Long projectId, LogTypeRequest logType) {
    logTypeValidator.validateUniqueness(projectId, logType.getName(), logType.getLevel());
    logTypeValidator.validateFilterableLimit(projectId, logType.getIsFilterable());
  }
}
