package com.epam.reportportal.base.core.aifactory.enums;

/**
 * Kind of an event of a stage trace.
 * <p>
 * Example: a Create attempt reports PROMPT, AGENT, TOOL_CALL, SELF_REVIEW, SELF_FIX, FINAL in this order; an
 * Upload stage, which has no agent, reports START, STEP, END.
 */
public enum TraceEventType {
  /** Input given to the agent, including the feedback of the previous attempt. */
  PROMPT,
  /** The agent's reasoning. */
  AGENT,
  /** A tool the agent called: its name in the title, the target and the duration in their own fields. */
  TOOL_CALL,
  /** Create only: the agent reviewing its own result. */
  SELF_REVIEW,
  /** Create only: the agent fixing what its self-review found. */
  SELF_FIX,
  /** The result of a grader for one case. */
  GRADERS,
  /** The result the agent returned to the orchestrator. */
  FINAL,
  /** Stages without an agent: the start. */
  START,
  /** Stages without an agent: a step. */
  STEP,
  /** Stages without an agent: the end. */
  END
}
