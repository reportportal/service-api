package com.epam.reportportal.base.core.tms.dto;

/**
 * Why a submitted Test Case was not sent to automation.
 * <p>
 * Example: TC43 was Ready when the editor selected it but became Draft a second before the click:
 * {@code NOT_READY}.
 */
public enum TmsAutomationSkipReason {
  /** The case is Draft. */
  NOT_READY,
  /** The agent is fixing the case (a fix round is pending). */
  FIX_RUNNING,
  /** The case is already in an automation iteration that has not ended. */
  AUTOMATION_IN_PROGRESS
}
