package com.epam.reportportal.base.core.tms.dto;

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
public class TmsTestCaseExecutionInTestPlanRS {
  private Long id;

  private TmsTestCaseExecutionLaunchRS launch;

  private String status;

  private Long startedAt;

  private Long finishedAt;

  private Long duration;
}
