package com.epam.reportportal.base.core.aifactory.event;

import lombok.Getter;

/**
 * Published after a stage retry has been successfully triggered on the CI side.
 */
@Getter
public class PipelineStageRetryRequestedEvent {
  private final Long stageId;
  private final Long iterationId;
  private final Long projectId;
  private final Long triggeredBy;
  private final String comment;

  public PipelineStageRetryRequestedEvent(Long stageId, Long iterationId, Long projectId, Long triggeredBy, String comment) {
    this.stageId = stageId;
    this.iterationId = iterationId;
    this.projectId = projectId;
    this.triggeredBy = triggeredBy;
    this.comment = comment;
  }
}
