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

package com.epam.ta.reportportal.core.logtype.validator;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

/**
 * Validator for log type operations. Encapsulates common validation logic shared between create and
 * update handlers.
 */
@Component
@RequiredArgsConstructor
public class LogTypeValidator {

  private static final int MAX_FILTERABLE_LOG_TYPES = 6;

  private final LogTypeRepository logTypeRepository;

  /**
   * Validates that the name and level combination is unique within the project.
   *
   * @param projectId The project ID
   * @param name      The name to validate
   * @param level     The level to validate
   */
  public void validateUniqueness(Long projectId, String name, Integer level) {
    expect(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId, name, level),
        BooleanUtils::isFalse)
        .verify(ErrorType.RESOURCE_ALREADY_EXISTS, String.format("Log type: %s - %s", name, level));
  }

  /**
   * Validates that creating or enabling filterable for a log type doesn't exceed the maximum
   * limit.
   *
   * @param projectId    The project ID
   * @param isFilterable Whether the log type should be filterable
   */
  public void validateFilterableLimit(Long projectId, Boolean isFilterable) {
    if (Boolean.TRUE.equals(isFilterable)) {
      expect(logTypeRepository.countFilterableLogTypes(projectId) < MAX_FILTERABLE_LOG_TYPES,
          BooleanUtils::isTrue)
          .verify(ErrorType.BAD_REQUEST_ERROR,
              String.format("Cannot create more than %s filterable log types per project.",
                  MAX_FILTERABLE_LOG_TYPES));
    }
  }

  public void validateLogTypeBelongsToProject(ProjectLogType logType, Long projectId) {
    expect(logType.getProjectId(), equalTo(projectId))
        .verify(ACCESS_DENIED, formattedSupplier(
            "Log type '{}' does not belong to the specified project", logType.getId()));
  }
}
