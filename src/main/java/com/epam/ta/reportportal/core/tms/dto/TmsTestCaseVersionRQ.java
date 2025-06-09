package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for test case version operations.
 * Contains information about a test case version, including its name, status flags,
 * links to requirements, and the manual scenario details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTestCaseVersionRQ {

  /**
   * Name of the test case version.
   */
  private String name;

  /**
   * Flag indicating whether this is the default version.
   */
  private Boolean isDefault;

  /**
   * Flag indicating whether this version is a draft.
   */
  private Boolean isDraft;

  /**
   * Links to requirements related to this test case.
   * Can be a comma-separated list of requirement identifiers.
   */
  private String linkToRequirements;

  /**
   * Manual scenario details for this test case version.
   * This can be either a step-based or text-based scenario.
   */
  private TmsManualScenarioRQ manualScenario;
}
