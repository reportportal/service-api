package com.epam.ta.reportportal.core.logtype;

import com.epam.reportportal.api.model.GetLogTypes200Response;

/**
 * Handles retrieval of log types for a given project.
 */
public interface GetLogTypeHandler {

  /**
   * Retrieves log types for the specified project.
   *
   * @param projectName The name of the project.
   * @return A response containing the log types for the project.
   */
  GetLogTypes200Response getLogTypes(String projectName);
}
