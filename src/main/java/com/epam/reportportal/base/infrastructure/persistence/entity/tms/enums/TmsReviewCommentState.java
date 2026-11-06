package com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums;

/**
 * Where a review comment stands on its way to the agent and back.
 * <p>
 * Example: a new comment is {@code NOT_SENT}; "Push to agent" makes it {@code SENT} (the agent is fixing); when
 * the fix round ends it is {@code ADDRESSED} or {@code NOT_ADDRESSED} (with the reason of the agent). A failed
 * round returns its comments to {@code NOT_SENT}.
 */
public enum TmsReviewCommentState {
  NOT_SENT,
  SENT,
  ADDRESSED,
  NOT_ADDRESSED
}
