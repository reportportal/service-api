package com.epam.reportportal.base.core.tms.dto.batch;

/**
 * Stable machine-readable reason for a per-test-case failure in a batch operation; clients should
 * branch on this rather than on the human-readable {@code errorMessage}. Absent for failures that
 * have no dedicated code.
 */
public enum BatchTestCaseOperationErrorReason {
  /** The test case does not exist in the project. */
  NOT_FOUND,
  /** The test case id appears more than once in the request. */
  DUPLICATE,
  /** The test case is already in the target (launch or test plan). */
  ALREADY_EXISTS,
  /** The test case is not Ready, so it cannot be added to a launch or test plan. */
  NOT_READY
}
