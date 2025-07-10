package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class TmsManualScenarioRS {

  private Long id;

  private Integer executionEstimationTime;

  private String linkToRequirements;

  private Set<TmsManualScenarioAttributeRS> attributes;

  private Set<TmsStepRS> steps;
}
