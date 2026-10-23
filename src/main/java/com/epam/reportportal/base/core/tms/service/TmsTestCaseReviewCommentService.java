package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseReviewRS;

/**
 * Review comments of AI test cases (story US-AI-FCTRY-011): an editor comments the precondition or a step, the
 * comments collect into one pending set per case until they are pushed to the agent or discarded.
 * <p>
 * Example: the editor adds "Name the exact toast text" to step 2 of TC103; {@link #getReview} then reports one
 * unsent comment, and the case cannot be approved until the comments are pushed or discarded.
 */
public interface TmsTestCaseReviewCommentService {

  /**
   * The comments of the test case with their states and where the review stands.
   */
  TmsTestCaseReviewRS getReview(long projectId, Long testCaseId);

  /**
   * Adds a comment; only to an AI case, and not while the agent is fixing it.
   */
  TmsReviewCommentRS addComment(long projectId, Long testCaseId, Long authorId, TmsReviewCommentRQ rq);

  /**
   * Deletes the own comment of the user while it is not sent.
   */
  void deleteOwnComment(long projectId, Long testCaseId, Long commentId, Long userId);

  /**
   * Removes all unsent comments of the test case.
   *
   * @return how many comments were removed
   */
  int discardUnsentComments(long projectId, Long testCaseId);
}
