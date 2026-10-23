package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_CASE_REVIEW_CONFLICT;

import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseReviewRS;
import com.epam.reportportal.base.core.tms.mapper.TmsReviewCommentMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentTarget;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseOrigin;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsTestCaseReviewCommentServiceImpl implements TmsTestCaseReviewCommentService {

  private static final String TEST_CASE_NOT_FOUND = "Test Case with id: %d for projectId: %d";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final TmsTestCaseFixPushRepository fixPushRepository;

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseReviewRS getReview(long projectId, Long testCaseId) {
    findTestCase(projectId, testCaseId);
    var comments = commentRepository.findByTestCaseIdOrderByCreatedAtAscIdAsc(testCaseId).stream()
        .map(TmsReviewCommentMapper::toRS)
        .toList();
    var unsent = comments.stream().filter(comment -> comment.getState() == TmsReviewCommentState.NOT_SENT).count();
    var pendingPush = fixPushRepository.findByTestCaseIdAndStatus(testCaseId, TmsFixPushStatus.PENDING);
    return TmsTestCaseReviewRS.builder()
        .comments(comments)
        .unsentCount((int) unsent)
        .fixInProgress(pendingPush.isPresent())
        .fixRound(pendingPush.map(push -> push.getRoundNumber()).orElse(null))
        .build();
  }

  @Override
  @Transactional
  public TmsReviewCommentRS addComment(long projectId, Long testCaseId, Long authorId, TmsReviewCommentRQ rq) {
    var testCase = findTestCase(projectId, testCaseId);
    assertAiCase(testCase);
    assertNotBeingFixed(testCaseId);
    validateTarget(rq);

    var comment = new TmsTestCaseReviewComment();
    comment.setTestCaseId(testCaseId);
    comment.setTargetType(rq.getTargetType());
    comment.setStepNumber(rq.getTargetType() == TmsReviewCommentTarget.STEP ? rq.getStepNumber() : null);
    comment.setCommentText(rq.getText().strip());
    comment.setAuthorId(authorId);
    comment.setCreatedAt(Instant.now());
    comment.setState(TmsReviewCommentState.NOT_SENT);
    return TmsReviewCommentMapper.toRS(commentRepository.save(comment));
  }

  @Override
  @Transactional
  public void deleteOwnComment(long projectId, Long testCaseId, Long commentId, Long userId) {
    findTestCase(projectId, testCaseId);
    var comment = commentRepository.findById(commentId)
        .filter(found -> found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Review comment '" + commentId + "'"));
    if (!comment.getAuthorId().equals(userId)) {
      throw new ReportPortalException(FORBIDDEN_OPERATION,
          "Only the author can delete a comment; the others are removed together with 'Discard comments'");
    }
    if (comment.getState() != TmsReviewCommentState.NOT_SENT) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT, "A comment that was pushed to the agent cannot be deleted");
    }
    commentRepository.delete(comment);
  }

  @Override
  @Transactional
  public int discardUnsentComments(long projectId, Long testCaseId) {
    findTestCase(projectId, testCaseId);
    assertNotBeingFixed(testCaseId);
    return commentRepository.deleteByTestCaseIdAndState(testCaseId, TmsReviewCommentState.NOT_SENT);
  }

  private TmsTestCase findTestCase(long projectId, Long testCaseId) {
    return testCaseRepository.findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, TEST_CASE_NOT_FOUND.formatted(testCaseId, projectId)));
  }

  private void assertAiCase(TmsTestCase testCase) {
    if (testCase.getOrigin() != TmsTestCaseOrigin.AI) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Review comments are available for AI test cases only");
    }
  }

  private void assertNotBeingFixed(Long testCaseId) {
    if (fixPushRepository.existsByTestCaseIdAndStatus(testCaseId, TmsFixPushStatus.PENDING)) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT,
          "The agent is fixing this Test Case; its comments are read-only until the fix round ends");
    }
  }

  private void validateTarget(TmsReviewCommentRQ rq) {
    var isStep = rq.getTargetType() == TmsReviewCommentTarget.STEP;
    if (isStep && rq.getStepNumber() == null) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "stepNumber is required for a STEP comment");
    }
    if (!isStep && rq.getStepNumber() != null) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "stepNumber is allowed for a STEP comment only");
    }
  }
}
