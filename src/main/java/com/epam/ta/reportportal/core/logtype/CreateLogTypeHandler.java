package com.epam.ta.reportportal.core.logtype;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;

/**
 * Interface for handling operations related to creating log types.
 */
public interface CreateLogTypeHandler {

  /**
   * Creates a new log type for the specified project.
   *
   * @param projectName The name of the project where the log type is to be created.
   * @param logType     The log type data containing details such as name, level, and visibility.
   * @param user        The user performing the action.
   * @return The newly created log type as a DTO.
   * @throws ReportPortalException if the project is not found or validation fails.
   */
  LogTypeResponse createLogType(String projectName, LogTypeRequest logType, ReportPortalUser user);
}
