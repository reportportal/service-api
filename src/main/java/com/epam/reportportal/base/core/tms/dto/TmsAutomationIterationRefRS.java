package com.epam.reportportal.base.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Points at an iteration of the Test automation pipeline. The id opens it at the pipeline iteration route, the
 * number is only the label.
 * <p>
 * Example: {@code {"pipelineId": 17, "iterationId": 452, "number": 8}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAutomationIterationRefRS {

  private Long pipelineId;
  private Long iterationId;
  private Integer number;
}
