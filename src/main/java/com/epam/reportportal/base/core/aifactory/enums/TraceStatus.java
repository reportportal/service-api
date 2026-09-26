package com.epam.reportportal.base.core.aifactory.enums;

/**
 * Whether the trace of a stage attempt reached ReportPortal.
 */
public enum TraceStatus {
  /** The events were reported. */
  REPORTED,
  /** The job could not report them; the reason is kept so the page can say why. */
  NOT_REPORTED
}
