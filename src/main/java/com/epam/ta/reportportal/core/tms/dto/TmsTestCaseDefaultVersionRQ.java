package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for default test case version.
 * Contains information about a test case default version, including its name, status flags,
 * links to requirements, and the manual scenario details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Valid
public class TmsTestCaseDefaultVersionRQ {

  /**
   * Name of the test case version.
   */
  private String name;

  /**
   * Manual scenario details for this test case version.
   * This can be either a step-based or text-based scenario.
   */
  @Valid
  private TmsManualScenarioRQ manualScenario;
}
