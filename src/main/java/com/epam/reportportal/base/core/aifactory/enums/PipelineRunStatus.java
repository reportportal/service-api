package com.epam.reportportal.base.core.aifactory.enums;

/**
 * Outcome of a pipeline iteration or an individual stage within it.
 */
public enum PipelineRunStatus {
  PENDING,
  PASSED,
  FAILED,
  NEEDS_HUMAN
}
