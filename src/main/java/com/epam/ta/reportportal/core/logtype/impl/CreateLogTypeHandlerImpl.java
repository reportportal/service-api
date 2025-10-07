package com.epam.ta.reportportal.core.logtype.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.reportportal.api.model.LogType;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.logtype.CreateLogTypeHandler;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.builders.LogTypeBuilder;
import com.epam.ta.reportportal.ws.converter.converters.LogTypeApiConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateLogTypeHandlerImpl implements CreateLogTypeHandler {

  private static final int MAX_FILTERABLE_LOG_TYPES = 6;

  private final ProjectRepository projectRepository;
  private final LogTypeRepository logTypeRepository;

  /**
   * Creates a new log type for a specific project after performing necessary validations.
   *
   * @param projectName The name of the project where the log type should be created.
   * @param logType     The log type payload containing the details of the log type to be created.
   * @return The newly created log type as a DTO.
   * @throws ReportPortalException if the project is not found, validation fails, or the log type
   *                               cannot be created.
   */
  @Override
  @Transactional
  public LogType createLogType(String projectName, LogType logType) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
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
    return LogTypeApiConverter.TO_RESOURCE.apply(savedEntity);
  }

  private void validateLogType(Long projectId, LogType logType) {
    validateUniquenessWithinProject(projectId, logType.getName(), logType.getLevel());
    validateFilterableLogTypeLimit(projectId, logType.getIsFilterable());
  }


  private void validateUniquenessWithinProject(Long projectId, String name, Integer level) {
    expect(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(projectId, name, level),
        BooleanUtils::isFalse)
        .verify(ErrorType.RESOURCE_ALREADY_EXISTS, String.format("Log type: %s - %s", name, level));
  }

  private void validateFilterableLogTypeLimit(Long projectId, Boolean isFilterable) {
    if (Boolean.TRUE.equals(isFilterable)) {
      expect(logTypeRepository.countFilterableLogTypes(projectId) < MAX_FILTERABLE_LOG_TYPES,
          BooleanUtils::isTrue).verify(ErrorType.BAD_REQUEST_ERROR,
          String.format("Cannot create more than %s filterable log types per project.",
              MAX_FILTERABLE_LOG_TYPES));
    }
  }

}
