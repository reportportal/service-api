package com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums;

/**
 * Where a fix round (one "Push to agent" of one test case) stands.
 * <p>
 * Example: a push is {@code PENDING} while the agent works; it ends {@code FIXED} when the new version of the
 * case arrives with a fresh grade, {@code FIXED_GRADE_FAILED} when the version arrives without one, or
 * {@code FAILED} when the job fails or does not answer in time.
 */
public enum TmsFixPushStatus {
  PENDING,
  FIXED,
  FIXED_GRADE_FAILED,
  FAILED
}
