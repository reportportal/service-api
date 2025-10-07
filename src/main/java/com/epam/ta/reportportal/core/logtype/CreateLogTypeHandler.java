package com.epam.ta.reportportal.core.logtype;

import com.epam.reportportal.api.model.LogType;
import com.epam.reportportal.rules.exception.ReportPortalException;

/**
 * Interface for handling operations related to creating log types.
 */
public interface CreateLogTypeHandler {

  /**
   * Creates a new log type for the specified project.
   *
   * @param projectName The name of the project where the log type is to be created.
   * @param logType     The log type data containing details such as name, level, and visibility.
   * @return The newly created log type as a DTO.
   * @throws ReportPortalException if the project is not found or validation fails.
   */
  LogType createLogType(String projectName, LogType logType);
}
