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

package com.epam.reportportal.core.logtype.validator;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;

import com.epam.reportportal.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

/**
 * Validates log type operations (create / update).
 */
@Component
@RequiredArgsConstructor
public class LogTypeValidator {

  private static final int MAX_FILTERABLE_LOG_TYPES = 6;
  private final LogTypeRepository logTypeRepository;

  /**
   * Checks that name–level pair is unique within a project.
   */
  public void validateUniqueness(Long projectId, String name, Integer level) {
    expect(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId, name, level),
        BooleanUtils::isFalse)
        .verify(ErrorType.RESOURCE_ALREADY_EXISTS, String.format("Log type: %s - %s", name, level));
  }

  /**
   * Ensures filterable count does not exceed {@value #MAX_FILTERABLE_LOG_TYPES}.
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

  /**
   * Verifies that given log type belongs to the specified project.
   */
  public void validateLogTypeBelongsToProject(ProjectLogType logType, Long projectId) {
    expect(logType.getProjectId(), equalTo(projectId))
        .verify(ACCESS_DENIED, formattedSupplier(
            "Log type '{}' does not belong to the specified project", logType.getId()));
  }
}
