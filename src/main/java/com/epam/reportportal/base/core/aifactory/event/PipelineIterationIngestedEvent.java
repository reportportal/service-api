package com.epam.reportportal.base.core.aifactory.event;

import lombok.Getter;

/**
 * Published after an ingested {@code PipelineIteration} (and its stages) has been
 * persisted, for activity/audit history and any future downstream consumer.
 */
@Getter
public class PipelineIterationIngestedEvent {
  private final Long iterationId;
  private final Long pipelineId;
  private final Long projectId;

  public PipelineIterationIngestedEvent(Long iterationId, Long pipelineId, Long projectId) {
    this.iterationId = iterationId;
    this.pipelineId = pipelineId;
    this.projectId = projectId;
  }
}
