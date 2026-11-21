package com.epam.reportportal.base.core.aifactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pointer to an artifact the stage produced (e.g. type LAUNCH and its id); tabular results are
 * carried by the stage's sections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepResultRS {
  private String resultType;
  private String resultRef;
}
