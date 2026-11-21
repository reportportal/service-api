package com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums;

/**
 * The part of a test case a review comment is attached to.
 * <p>
 * Example: on a case with the Steps template a comment on step 2 is {@code STEP} with step number 2 and a
 * comment on the precondition row is {@code PRECONDITION}; on a Text case the instructions / expected result
 * block is {@code SCENARIO_TEXT}.
 */
public enum TmsReviewCommentTarget {
  PRECONDITION,
  STEP,
  SCENARIO_TEXT
}
