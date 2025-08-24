package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestCaseRS {

  private Long id;

  private String name;

  private String description;

  private String priority;

  private Long createdAt;

  private Long updatedAt;

  private TmsTestCaseTestFolderRS testFolder;

  private TmsManualScenarioRS manualScenario;

  private Set<TmsAttributeRS> tags;
}
