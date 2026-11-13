package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import lombok.Data;

/**
 * DTO for deserializing test case snapshot from JSON.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmsTestCaseSnapshotDTO {

  private Long id;
  private String name;
  private String description;
  private String priority;
  private String displayId;
  private TmsTestCaseTestFolderRS testFolder;
  private TmsManualScenarioRS manualScenario;
  private Set<TmsTestCaseExecutionAttributeRS> attributes;
}
