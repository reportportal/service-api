package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Tells whether a test case may be made Ready right now, either by a person (Approve) or by Auto-Ready.
 * <p>
 * Example: TC103 has one comment that is not sent yet. Approve is rejected with "Push or discard the review
 * comments first", and Auto-Ready skips it even if its score is above the threshold. The same holds while the
 * agent is fixing the case (a pushed round is running), until the fixed version arrives.
 */
@Service
@RequiredArgsConstructor
public class TmsTestCaseReadyEligibilityService {

  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final TmsTestCaseFixRoundRepository fixRoundRepository;

  /**
   * Which of the given test cases must not become Ready now: the ones with unsent review comments and the ones
   * the agent is fixing.
   */
  public Set<Long> findCasesThatCannotBecomeReady(Collection<Long> testCaseIds) {
    if (testCaseIds.isEmpty()) {
      return Set.of();
    }
    var blocked = new HashSet<Long>(commentRepository.findTestCaseIdsWithState(testCaseIds, TmsReviewCommentState.NOT_SENT));
    blocked.addAll(fixRoundRepository.findTestCaseIdsWithStatus(testCaseIds, TmsFixRoundStatus.PENDING));
    return blocked;
  }

  public boolean canBecomeReady(Long testCaseId) {
    return findCasesThatCannotBecomeReady(List.of(testCaseId)).isEmpty();
  }

  /**
   * Rejects making the test case Ready when it has unsent comments or is being fixed.
   */
  public void assertCanBecomeReady(Long testCaseId) {
    if (fixRoundRepository.existsByTestCaseIdAndStatus(testCaseId, TmsFixRoundStatus.PENDING)) {
      throw new ReportPortalException(ErrorType.TEST_CASE_REVIEW_CONFLICT,
          "The agent is fixing Test Case '" + testCaseId + "'; wait for the fix round to end");
    }
    if (commentRepository.countByTestCaseIdAndState(testCaseId, TmsReviewCommentState.NOT_SENT) > 0) {
      throw new ReportPortalException(ErrorType.TEST_CASE_REVIEW_CONFLICT,
          "Push or discard the review comments of Test Case '" + testCaseId + "' first");
    }
  }
}
