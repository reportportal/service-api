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

package com.epam.ta.reportportal.core.logtype.impl;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.logtype.UpdateLogTypeHandler;
import com.epam.ta.reportportal.core.logtype.validator.LogTypeValidator;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.builders.LogTypeBuilder;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UpdateLogTypeHandler} for updating log types.
 */
@Service
@RequiredArgsConstructor
public class UpdateLogTypeHandlerImpl implements UpdateLogTypeHandler {

  private final ProjectRepository projectRepository;
  private final LogTypeRepository logTypeRepository;
  private final LogTypeValidator logTypeValidator;

  /**
   * Updates a log type by ID in the specified project.
   *
   * @param projectName The name of the project.
   * @param logTypeId   The ID of the log type to update.
   * @param updateRq    The log type data containing the updated fields.
   * @return SuccessfulUpdate
   * @throws ReportPortalException if the project or log type is not found, or validation fails.
   */
  @Override
  @Transactional
  public SuccessfulUpdate updateLogType(String projectName, Long logTypeId,
      LogTypeRequest updateRq) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

    ProjectLogType existingLogType = logTypeRepository.findById(logTypeId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Log type"));

    logTypeValidator.validateLogTypeBelongsToProject(existingLogType, project.getId());
    validateUpdate(existingLogType, updateRq, project.getId());

    ProjectLogType updatedLogType = new LogTypeBuilder(existingLogType)
        .addUpdateRq(updateRq)
        .get();

    logTypeRepository.save(updatedLogType);

    return new SuccessfulUpdate("The update was completed successfully.");
  }

  private void validateUpdate(ProjectLogType existingLogType, LogTypeRequest updateRq,
      Long projectId) {
    validateSystemLogTypeRestrictions(existingLogType, updateRq);
    validateNameAndLevelUniqueness(existingLogType, updateRq, projectId);
    validateFilterableConstraints(existingLogType, updateRq, projectId);
  }

  private void validateSystemLogTypeRestrictions(ProjectLogType existingLogType,
      LogTypeRequest updateRq) {
    if (!existingLogType.isSystem()) {
      return;
    }

    if (isUpdated(existingLogType.getName(), updateRq.getName())) {
      throw new ReportPortalException(
          ErrorType.ACCESS_DENIED,
          String.format("Cannot modify name of system log type '%s'", existingLogType.getName())
      );
    }

    if (isUpdated(existingLogType.getLevel(), updateRq.getLevel())) {
      throw new ReportPortalException(
          ErrorType.ACCESS_DENIED,
          String.format("Cannot modify level of system log type '%s'", existingLogType.getName())
      );
    }
  }

  private void validateNameAndLevelUniqueness(ProjectLogType existingLogType,
      LogTypeRequest updateRq,
      Long projectId) {
    var nameUpdated = isUpdated(existingLogType.getName(), updateRq.getName());
    var levelUpdated = isUpdated(existingLogType.getLevel(), updateRq.getLevel());

    if (nameUpdated || levelUpdated) {
      var nameToValidate = nameUpdated ? updateRq.getName() : existingLogType.getName();
      var levelToValidate = levelUpdated ? updateRq.getLevel() : existingLogType.getLevel();
      logTypeValidator.validateUniquenessExcludingId(projectId, nameToValidate, levelToValidate,
          existingLogType.getId());
    }
  }

  private void validateFilterableConstraints(ProjectLogType existingLogType,
      LogTypeRequest updateRq,
      Long projectId) {

    if (Boolean.TRUE.equals(updateRq.getIsFilterable())) {
      validateUnknownLogType(existingLogType);

      if (!existingLogType.isFilterable()) {
        logTypeValidator.validateFilterableLimit(projectId, true);
      }
    }
  }

  private void validateUnknownLogType(ProjectLogType existingLogType) {
    if (LogLevel.UNKNOWN.toString().equalsIgnoreCase(existingLogType.getName())) {
      throw new ReportPortalException(
          ErrorType.ACCESS_DENIED,
          "The 'unknown' log type cannot be set as filterable"
      );
    }
  }

  private boolean isUpdated(Object existingField, Object newField) {
    return !Objects.equals(existingField, newField);
  }
}
