package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestCaseVersionRS {

  private Long id;

  private String name;

  private Boolean isDefault;

  private Boolean isDraft;

  private TmsManualScenarioRS manualScenario;
}
