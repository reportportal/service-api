package com.epam.ta.reportportal.core.logtype.impl;

import com.epam.reportportal.api.model.GetLogTypes200Response;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.logtype.GetLogTypeHandler;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.converters.LogTypeConverter;
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
   * @param projectName The name of the project.
   * @return A response containing the log types for the project.
   * @throws ReportPortalException If the project is not found.
   */
  @Override
  public GetLogTypes200Response getLogTypes(String projectName) {
    Project project = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

    List<LogTypeResponse> items = logTypeRepository.findByProjectId(project.getId()).stream()
        .map(LogTypeConverter.TO_RESOURCE)
        .toList();

    return new GetLogTypes200Response(items);
  }
}
