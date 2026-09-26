package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceAttemptRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRS;
import java.util.List;

/**
 * Stores and serves the trace of stage attempts (what the agent was given, how it reasoned, which tools it
 * called, what it returned). The trace is a record of the pipeline: it lives and is deleted with its iteration.
 */
public interface PipelineTraceService {

  /**
   * Stores the events of an attempt, or notes that the job could not report them. Repeating a request changes
   * nothing: an event with a seq that is already stored replaces it. The request's attempt must be 1 up to the
   * stage's current attempt.
   */
  void reportTrace(Long projectId, Long iterationId, String stageKey, PipelineTraceRQ rq);

  /**
   * The events of one attempt, with their texts.
   *
   * @param attempt the attempt to load, or null for the stage's current one
   */
  PipelineTraceRS getTrace(Long projectId, Long iterationId, String stageKey, Integer attempt);

  /**
   * All attempts of the stage, oldest first, with whether each has a trace.
   */
  List<PipelineTraceAttemptRS> getTraceAttempts(Long projectId, Long iterationId, String stageKey);
}
