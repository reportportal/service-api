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

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.events.domain.LogTypeDeletedEvent;
import com.epam.reportportal.base.core.logtype.DeleteLogTypeHandler;
import com.epam.reportportal.base.core.logtype.validator.LogTypeValidator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.converter.converters.LogTypeConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link DeleteLogTypeHandler} for deleting log types.
 */
@Service
@RequiredArgsConstructor
public class DeleteLogTypeHandlerImpl implements DeleteLogTypeHandler {

  private final ProjectRepository projectRepository;
  private final LogTypeRepository logTypeRepository;
  private final LogTypeValidator logTypeValidator;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Deletes a log type by ID from the specified project.
   *
   * @param projectKey The key of the project.
   * @param logTypeId  The ID of the log type to delete.
   * @param user       The user performing the action.
   * @throws ReportPortalException if the project or log type is not found, or if the log type is a system log type.
   */
  @Override
  @Transactional
  public void deleteLogType(String projectKey, Long logTypeId, ReportPortalUser user) {
    Project project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectKey));

    ProjectLogType logType = logTypeRepository.findById(logTypeId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Log type"));

    validate(logType, project);

    logTypeRepository.delete(logType);

    eventPublisher.publishEvent(
        new LogTypeDeletedEvent(LogTypeConverter.TO_ACTIVITY_RESOURCE.apply(logType),
            user.getUserId(), user.getUsername()
        ));
  }

  private void validate(ProjectLogType logType, Project project) {
    logTypeValidator.validateLogTypeBelongsToProject(logType, project.getId());

    validateNotSystemLogType(logType);
  }

  private void validateNotSystemLogType(ProjectLogType logType) {
    expect(logType.isSystem(), BooleanUtils::isFalse)
        .verify(ACCESS_DENIED,
            String.format("Cannot delete system log type '%s'", logType.getName())
        );
  }
}
