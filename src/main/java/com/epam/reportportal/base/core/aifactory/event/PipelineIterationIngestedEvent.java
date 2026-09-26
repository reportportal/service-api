package com.epam.reportportal.base.core.aifactory.event;

import lombok.Getter;

/**
 * Published after an ingested {@code PipelineIteration} (and its stages) has been
 * persisted, intended for activity/audit history and any future downstream
 * consumer. No {@code EventToActivityConverter} is registered for this event yet
 * (see {@code EventObject.PIPELINE_ITERATION}) — publishing it today has no
 * observable effect. Wiring an activity converter, following the pattern of
 * {@code TestCaseCreatedEventConverter}, is a tracked follow-up.
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
