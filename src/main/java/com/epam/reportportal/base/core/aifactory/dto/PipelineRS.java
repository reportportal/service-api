package com.epam.reportportal.base.core.aifactory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A pipeline definition (A.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineRS {
  private Long id;
  private String name;
  private String description;
  private boolean autoReadyEnabled;
  private Integer autoReadyThreshold;
  private long iterationsCount;
  private PipelineIterationSummaryRS latestIteration;
  private Instant createdAt;
}
