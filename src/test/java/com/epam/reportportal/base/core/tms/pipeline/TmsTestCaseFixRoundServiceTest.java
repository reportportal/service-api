package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsFixRoundResultRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseQualityScoreRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseQualityService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseFixRoundServiceTest {

  private static final long PROJECT_ID = 1L;
  private static final Long TEST_CASE_ID = 103L;
  private static final Long FIX_ROUND_ID = 7L;
  private static final Long PREVIOUS_ITERATION_ID = 41L;

  @Mock
  private TmsTestCaseRepository testCaseRepository;
  @Mock
  private TmsTestCaseFixRoundRepository fixRoundRepository;
  @Mock
  private TmsTestCaseReviewCommentRepository commentRepository;
  @Mock
  private PipelineIterationRepository iterationRepository;
  @Mock
  private TmsTestCaseVersionService versionService;
  @Mock
  private TmsTestCaseQualityService qualityService;
  @Mock
  private TmsTestCaseService testCaseService;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private TmsTestCaseFixRoundService sut;

  private TmsTestCase testCase;
  private TmsTestCaseFixRound round;
  private TmsTestCaseVersion newVersion;
  private TmsTestCaseReviewComment applied;
  private TmsTestCaseReviewComment notApplied;
  private TmsTestCaseReviewComment unanswered;

  @BeforeEach
  void setUp() {
    testCase = new TmsTestCase();
    testCase.setId(TEST_CASE_ID);
    testCase.setName("Old title");
    testCase.setStatus(TmsTestCaseStatus.READY);

    round = new TmsTestCaseFixRound();
    round.setId(FIX_ROUND_ID);
    round.setProjectId(PROJECT_ID);
    round.setTestCaseId(TEST_CASE_ID);
    round.setRoundNumber(1);
    round.setStatus(TmsFixRoundStatus.PENDING);
    round.setPreviousIterationId(PREVIOUS_ITERATION_ID);

    newVersion = new TmsTestCaseVersion();
    applied = sentComment(11L);
    notApplied = sentComment(12L);
    unanswered = sentComment(13L);
  }

  private static TmsTestCaseReviewComment sentComment(Long id) {
    var comment = new TmsTestCaseReviewComment();
    comment.setId(id);
    comment.setTestCaseId(TEST_CASE_ID);
    comment.setState(TmsReviewCommentState.SENT);
    comment.setFixRoundId(FIX_ROUND_ID);
    return comment;
  }

  private TmsFixRoundResultRQ.CommentResult result(Long commentId, boolean addressed, String reason) {
    return TmsFixRoundResultRQ.CommentResult.builder().commentId(commentId).addressed(addressed).reason(reason).build();
  }

  private TmsFixRoundResultRQ resultOf(List<TmsTestCaseQualityScoreRQ> scores) {
    return TmsFixRoundResultRQ.builder()
        .pipelineIterationId(52L)
        .manualScenario(org.mockito.Mockito.mock(TmsManualScenarioRQ.class))
        .name("New title")
        .qualityScores(scores)
        .commentResults(List.of(result(11L, true, null), result(12L, false, "Needs a new case")))
        .build();
  }

  private void givenAPendingRoundOfAnExistingCase() {
    when(fixRoundRepository.findByIdForUpdate(FIX_ROUND_ID)).thenReturn(Optional.of(round));
    when(testCaseRepository.findByProjectIdAndId(PROJECT_ID, TEST_CASE_ID)).thenReturn(Optional.of(testCase));
    when(versionService.createNewDefaultVersion(eq(PROJECT_ID), eq(testCase), any(), eq(52L))).thenReturn(newVersion);
    when(commentRepository.findByFixRoundId(FIX_ROUND_ID)).thenReturn(List.of(applied, notApplied, unanswered));
    when(testCaseService.getById(PROJECT_ID, TEST_CASE_ID)).thenReturn(new TmsTestCaseRS());
  }

  private void givenAutoReady(int threshold) {
    var pipeline = new Pipeline();
    pipeline.setSettings(new PipelineSettings(Map.of("autoReady", Map.of("enabled", true, "threshold", threshold))));
    var iteration = new PipelineIteration();
    iteration.setPipeline(pipeline);
    when(iterationRepository.findById(PREVIOUS_ITERATION_ID)).thenReturn(Optional.of(iteration));
  }

  @Test
  void applyResult_ShouldCreateTheNewVersionGradeItAndSendTheCaseBackToDraft() {
    givenAPendingRoundOfAnExistingCase();
    var scores = List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build());
    var rq = resultOf(scores);

    sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID, rq);

    verify(versionService).createNewDefaultVersion(PROJECT_ID, testCase, rq.getManualScenario(), 52L);
    verify(qualityService).applyQualityScores(PROJECT_ID, newVersion, scores);
    verify(qualityService).applyGenerationMetadata(newVersion, rq.getGeneration());
    assertEquals(TmsTestCaseStatus.DRAFT, testCase.getStatus());
    assertTrue(testCase.isModifiedByAgent());
    assertEquals("New title", testCase.getName());
    assertEquals(TmsFixRoundStatus.FIXED, round.getStatus());
    assertEquals(52L, round.getResultIterationId());
    assertNotNull(round.getFinishedAt());
  }

  @Test
  void applyResult_ShouldMarkEachCommentWithItsAnswerAndTheRound() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID,
        resultOf(List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build())));

    assertEquals(TmsReviewCommentState.ADDRESSED, applied.getState());
    assertEquals(1, applied.getRoundNumber());
    assertNull(applied.getAgentReason());
    assertEquals(TmsReviewCommentState.NOT_ADDRESSED, notApplied.getState());
    assertEquals("Needs a new case", notApplied.getAgentReason());
    assertEquals(TmsReviewCommentState.NOT_ADDRESSED, unanswered.getState());
    assertEquals(TmsTestCaseFixRoundService.UNANSWERED_REASON, unanswered.getAgentReason());
  }

  @Test
  void applyResult_WithAGrade_ShouldRunAutoReadyWithTheThresholdOfThePipeline() {
    givenAPendingRoundOfAnExistingCase();
    givenAutoReady(90);

    sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID,
        resultOf(List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build())));

    verify(testCaseService).applyAutoReady(PROJECT_ID, TEST_CASE_ID, 90);
  }

  @Test
  void applyResult_WithoutAGrade_ShouldEndAsFixedGradeFailedAndNotRunAutoReady() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID, resultOf(null));

    assertEquals(TmsFixRoundStatus.FIXED_GRADE_FAILED, round.getStatus());
    assertEquals(TmsTestCaseStatus.DRAFT, testCase.getStatus());
    verify(testCaseService, never()).applyAutoReady(anyLong(), anyLong(), anyInt());
  }

  @Test
  void applyResult_ShouldTellTheReviewOfTheIterationsThatTheLifecycleChanged() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID, resultOf(null));

    var event = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertEquals(new TmsTestCasesLifecycleChangedEvent(List.of(TEST_CASE_ID)), event.getValue());
  }

  @Test
  void applyResult_WhenTheRoundHasAlreadyEnded_ShouldBeAConflictAndChangeNothing() {
    round.setStatus(TmsFixRoundStatus.FAILED);
    when(fixRoundRepository.findByIdForUpdate(FIX_ROUND_ID)).thenReturn(Optional.of(round));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID, resultOf(null)));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
    verify(versionService, never()).createNewDefaultVersion(anyLong(), any(), any(), any());
  }

  @Test
  void applyResult_ForARoundOfAnotherTestCase_ShouldBeNotFound() {
    round.setTestCaseId(999L);
    when(fixRoundRepository.findByIdForUpdate(FIX_ROUND_ID)).thenReturn(Optional.of(round));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyResult(PROJECT_ID, TEST_CASE_ID, FIX_ROUND_ID, resultOf(null)));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void failRound_ShouldReturnTheCommentsToNotSentAndEndTheRoundAsFailed() {
    when(fixRoundRepository.findByIdForUpdate(FIX_ROUND_ID)).thenReturn(Optional.of(round));
    when(commentRepository.findByFixRoundId(FIX_ROUND_ID)).thenReturn(List.of(applied, notApplied));

    sut.failRound(FIX_ROUND_ID, "Stage 'Fix' failed");

    assertEquals(TmsFixRoundStatus.FAILED, round.getStatus());
    assertEquals("Stage 'Fix' failed", round.getFailureReason());
    assertNotNull(round.getFinishedAt());
    assertEquals(TmsReviewCommentState.NOT_SENT, applied.getState());
    assertNull(applied.getFixRoundId());
    assertEquals(TmsReviewCommentState.NOT_SENT, notApplied.getState());
  }

  @Test
  void failRound_WhenTheRoundAlreadyEnded_ShouldDoNothing() {
    round.setStatus(TmsFixRoundStatus.FIXED);
    when(fixRoundRepository.findByIdForUpdate(FIX_ROUND_ID)).thenReturn(Optional.of(round));

    sut.failRound(FIX_ROUND_ID, "late");

    assertEquals(TmsFixRoundStatus.FIXED, round.getStatus());
    verify(commentRepository, never()).save(any());
  }
}
