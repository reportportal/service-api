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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.logtype.DeleteLogTypeHandler;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.entity.project.Project;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
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

  /**
   * Deletes a log type by ID from the specified project.
   *
   * @param projectName The name of the project.
   * @param logTypeId   The ID of the log type to delete.
   * @param user        The user performing the deletion.
   * @throws ReportPortalException if the project or log type is not found, or if the log type is a system log type.
   */
  @Override
  @Transactional
  public void deleteLogType(String projectName, Long logTypeId, ReportPortalUser user) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

    ProjectLogType logType = logTypeRepository.findById(logTypeId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Log type"));

    validate(logType, project, user);

    logTypeRepository.delete(logType);
  }

  private void validate(ProjectLogType logType, Project project, ReportPortalUser user) {
    validateLogTypeBelongsToProject(logType, project.getId());
    validateNotSystemLogType(logType);
  }

  private void validateLogTypeBelongsToProject(ProjectLogType logType, Long projectId) {
    expect(logType.getProjectId(), equalTo(projectId))
        .verify(ACCESS_DENIED, formattedSupplier(
            "Log type '{}' does not belong to the specified project", logType.getId()));
  }

  private void validateNotSystemLogType(ProjectLogType logType) {
    expect(logType.isSystem(), BooleanUtils::isFalse)
        .verify(ACCESS_DENIED,
            String.format("Cannot delete system log type '%s'", logType.getName())
        );
  }
}
