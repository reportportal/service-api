package com.epam.reportportal.base.core.aifactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reference to another iteration of the same pipeline, e.g. the one a re-run was started from.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIterationRefRS {
  private Long id;
  private Integer iterationNumber;
}
