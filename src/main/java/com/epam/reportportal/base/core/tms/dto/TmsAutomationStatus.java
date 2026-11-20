package com.epam.reportportal.base.core.tms.dto;

/**
 * Where a Test Case is in automation (US-017).
 * <p>
 * Example: a Ready case nobody sent is {@code NOT_AUTOMATED}; after Automate it is {@code IN_PROGRESS} until its
 * automation iteration ends, then {@code AUTOMATED} (the iteration passed) or {@code FAILED}.
 */
public enum TmsAutomationStatus {
  NOT_AUTOMATED,
  IN_PROGRESS,
  AUTOMATED,
  FAILED
}
