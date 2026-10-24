package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentTarget;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseOrigin;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseReviewCommentServiceImplTest {

  private static final long PROJECT_ID = 1L;
  private static final Long TEST_CASE_ID = 103L;
  private static final Long USER_ID = 5L;

  @Mock
  private TmsTestCaseRepository testCaseRepository;
  @Mock
  private TmsTestCaseReviewCommentRepository commentRepository;
  @Mock
  private TmsTestCaseFixRoundRepository fixRoundRepository;

  @InjectMocks
  private TmsTestCaseReviewCommentServiceImpl sut;

  private TmsTestCase aiCase;

  @BeforeEach
  void setUp() {
    aiCase = new TmsTestCase();
    aiCase.setId(TEST_CASE_ID);
    aiCase.setOrigin(TmsTestCaseOrigin.AI);
    when(testCaseRepository.findByProjectIdAndId(PROJECT_ID, TEST_CASE_ID)).thenReturn(Optional.of(aiCase));
  }

  private static TmsReviewCommentRQ stepComment(int stepNumber, String text) {
    return TmsReviewCommentRQ.builder().targetType(TmsReviewCommentTarget.STEP).stepNumber(stepNumber).text(text).build();
  }

  private static TmsTestCaseReviewComment comment(Long id, Long authorId, TmsReviewCommentState state) {
    var comment = new TmsTestCaseReviewComment();
    comment.setId(id);
    comment.setTestCaseId(TEST_CASE_ID);
    comment.setTargetType(TmsReviewCommentTarget.STEP);
    comment.setStepNumber(2);
    comment.setCommentText("Name the exact toast text");
    comment.setAuthorId(authorId);
    comment.setCreatedAt(Instant.parse("2026-10-20T10:00:00Z"));
    comment.setState(state);
    return comment;
  }

  @Test
  void addComment_OnAnAiCase_ShouldStoreItAsNotSentWithTheAuthor() {
    when(commentRepository.save(any(TmsTestCaseReviewComment.class))).thenAnswer(call -> call.getArgument(0));

    var created = sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, stepComment(2, "  Name the exact toast text "));

    assertEquals(TmsReviewCommentState.NOT_SENT, created.getState());
    assertEquals(TmsReviewCommentTarget.STEP, created.getTargetType());
    assertEquals(2, created.getStepNumber());
    assertEquals("Name the exact toast text", created.getText());
    assertEquals(USER_ID, created.getAuthorId());
  }

  @Test
  void addComment_OnAPrecondition_ShouldNotKeepAStepNumber() {
    when(commentRepository.save(any(TmsTestCaseReviewComment.class))).thenAnswer(call -> call.getArgument(0));
    var rq = TmsReviewCommentRQ.builder().targetType(TmsReviewCommentTarget.PRECONDITION).text("Split this case").build();

    var created = sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, rq);

    assertNull(created.getStepNumber());
  }

  @Test
  void addComment_OnAManualCase_ShouldBeRejected() {
    aiCase.setOrigin(TmsTestCaseOrigin.MANUAL);

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, stepComment(1, "text")));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    verify(commentRepository, never()).save(any());
  }

  @Test
  void addComment_WhileTheAgentIsFixingTheCase_ShouldBeRejectedAsAConflict() {
    when(fixRoundRepository.existsByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixRoundStatus.PENDING)).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, stepComment(1, "text")));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void addComment_OnAStepWithoutItsNumber_ShouldBeRejected() {
    var rq = TmsReviewCommentRQ.builder().targetType(TmsReviewCommentTarget.STEP).text("text").build();

    var ex = assertThrows(ReportPortalException.class, () -> sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, rq));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void addComment_OnAPreconditionWithAStepNumber_ShouldBeRejected() {
    var rq = TmsReviewCommentRQ.builder().targetType(TmsReviewCommentTarget.PRECONDITION).stepNumber(1).text("text").build();

    assertThrows(ReportPortalException.class, () -> sut.addComment(PROJECT_ID, TEST_CASE_ID, USER_ID, rq));
  }

  @Test
  void getReview_ShouldCountTheUnsentCommentsAndTellWhetherTheAgentIsFixing() {
    when(commentRepository.findByTestCaseIdOrderByCreatedAtAscIdAsc(TEST_CASE_ID)).thenReturn(List.of(
        comment(1L, USER_ID, TmsReviewCommentState.NOT_SENT),
        comment(2L, USER_ID, TmsReviewCommentState.NOT_SENT),
        comment(3L, USER_ID, TmsReviewCommentState.ADDRESSED)));
    var push = new TmsTestCaseFixRound();
    push.setRoundNumber(2);
    when(fixRoundRepository.findByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixRoundStatus.PENDING))
        .thenReturn(Optional.of(push));

    var review = sut.getReview(PROJECT_ID, TEST_CASE_ID);

    assertEquals(3, review.getComments().size());
    assertEquals(2, review.getUnsentCount());
    assertTrue(review.isFixInProgress());
    assertEquals(2, review.getFixRound());
  }

  @Test
  void getReview_WhenNothingIsRunning_ShouldNotReportAFixRound() {
    when(commentRepository.findByTestCaseIdOrderByCreatedAtAscIdAsc(TEST_CASE_ID)).thenReturn(List.of());
    when(fixRoundRepository.findByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixRoundStatus.PENDING)).thenReturn(Optional.empty());

    var review = sut.getReview(PROJECT_ID, TEST_CASE_ID);

    assertFalse(review.isFixInProgress());
    assertNull(review.getFixRound());
    assertEquals(0, review.getUnsentCount());
  }

  @Test
  void deleteOwnComment_ShouldRemoveACommentOfTheUserThatIsNotSentYet() {
    var own = comment(1L, USER_ID, TmsReviewCommentState.NOT_SENT);
    when(commentRepository.findById(1L)).thenReturn(Optional.of(own));

    sut.deleteOwnComment(PROJECT_ID, TEST_CASE_ID, 1L, USER_ID);

    verify(commentRepository).delete(own);
  }

  @Test
  void deleteOwnComment_OfSomeoneElse_ShouldBeForbidden() {
    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment(1L, 99L, TmsReviewCommentState.NOT_SENT)));

    var ex = assertThrows(ReportPortalException.class, () -> sut.deleteOwnComment(PROJECT_ID, TEST_CASE_ID, 1L, USER_ID));

    assertEquals(ErrorType.FORBIDDEN_OPERATION.getCode(), ex.getErrorType().getCode());
    verify(commentRepository, never()).delete(any());
  }

  @Test
  void deleteOwnComment_ThatWasPushedToTheAgent_ShouldBeRejected() {
    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment(1L, USER_ID, TmsReviewCommentState.SENT)));

    var ex = assertThrows(ReportPortalException.class, () -> sut.deleteOwnComment(PROJECT_ID, TEST_CASE_ID, 1L, USER_ID));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void deleteOwnComment_OfAnotherTestCase_ShouldBeNotFound() {
    var other = comment(1L, USER_ID, TmsReviewCommentState.NOT_SENT);
    other.setTestCaseId(999L);
    when(commentRepository.findById(1L)).thenReturn(Optional.of(other));

    var ex = assertThrows(ReportPortalException.class, () -> sut.deleteOwnComment(PROJECT_ID, TEST_CASE_ID, 1L, USER_ID));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void discardUnsentComments_ShouldRemoveOnlyTheNotSentOnes() {
    when(commentRepository.deleteByTestCaseIdAndState(TEST_CASE_ID, TmsReviewCommentState.NOT_SENT)).thenReturn(2);

    var removed = sut.discardUnsentComments(PROJECT_ID, TEST_CASE_ID);

    assertEquals(2, removed);
    var state = ArgumentCaptor.forClass(TmsReviewCommentState.class);
    verify(commentRepository).deleteByTestCaseIdAndState(any(), state.capture());
    assertEquals(TmsReviewCommentState.NOT_SENT, state.getValue());
  }

  @Test
  void discardUnsentComments_WhileTheAgentIsFixingTheCase_ShouldBeRejected() {
    when(fixRoundRepository.existsByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixRoundStatus.PENDING)).thenReturn(true);

    assertThrows(ReportPortalException.class, () -> sut.discardUnsentComments(PROJECT_ID, TEST_CASE_ID));

    verify(commentRepository, never()).deleteByTestCaseIdAndState(any(), any());
  }
}
