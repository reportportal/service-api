package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Iteration detail (A.4): the A.2 row shape plus its ordered stages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineIterationDetailRS {
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
  private Instant startedAt;
  private Instant finishedAt;
  private Long createdBy;
  private Instant createdAt;
  private List<PipelineStageRS> stages;
}
