package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Row shape for a pipeline iteration: used both as {@code latestIteration} on
 * {@link PipelineRS} and as one entry of the paged iterations list (A.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineIterationSummaryRS {
  private Long id;
  private Long pipelineId;
  private String pipelineName;
  private Integer iterationNumber;
  private PipelineRunStatus status;
  private PipelineRunStatus qualityGate;
  private Map<String, Object> metrics;
  private String trigger;
  private boolean rerun;
  private Long rerunOfIterationId;
  private Integer stagesCount;
  private Instant startedAt;
  private Instant finishedAt;
  private Long createdBy;
  private Instant createdAt;
}
