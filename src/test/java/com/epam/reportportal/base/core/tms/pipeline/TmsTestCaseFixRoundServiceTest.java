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
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseFixedVersionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseQualityScoreRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseQualityService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixPush;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
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
  private static final Long PUSH_ID = 7L;
  private static final Long PREVIOUS_ITERATION_ID = 41L;

  @Mock
  private TmsTestCaseRepository testCaseRepository;
  @Mock
  private TmsTestCaseFixPushRepository fixPushRepository;
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
  private TmsTestCaseFixPush push;
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

    push = new TmsTestCaseFixPush();
    push.setId(PUSH_ID);
    push.setProjectId(PROJECT_ID);
    push.setTestCaseId(TEST_CASE_ID);
    push.setRoundNumber(1);
    push.setStatus(TmsFixPushStatus.PENDING);
    push.setPreviousIterationId(PREVIOUS_ITERATION_ID);

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
    comment.setFixPushId(PUSH_ID);
    return comment;
  }

  private TmsTestCaseFixedVersionRQ.CommentResult result(Long commentId, boolean addressed, String reason) {
    return TmsTestCaseFixedVersionRQ.CommentResult.builder().commentId(commentId).addressed(addressed).reason(reason).build();
  }

  private TmsTestCaseFixedVersionRQ fixedVersion(List<TmsTestCaseQualityScoreRQ> scores) {
    return TmsTestCaseFixedVersionRQ.builder()
        .fixPushId(PUSH_ID)
        .pipelineIterationId(52L)
        .manualScenario(org.mockito.Mockito.mock(TmsManualScenarioRQ.class))
        .name("New title")
        .qualityScores(scores)
        .commentResults(List.of(result(11L, true, null), result(12L, false, "Needs a new case")))
        .build();
  }

  private void givenAPendingRoundOfAnExistingCase() {
    when(fixPushRepository.findByIdForUpdate(PUSH_ID)).thenReturn(Optional.of(push));
    when(testCaseRepository.findByProjectIdAndId(PROJECT_ID, TEST_CASE_ID)).thenReturn(Optional.of(testCase));
    when(versionService.createNewDefaultVersion(eq(PROJECT_ID), eq(testCase), any(), eq(52L))).thenReturn(newVersion);
    when(commentRepository.findByFixPushId(PUSH_ID)).thenReturn(List.of(applied, notApplied, unanswered));
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
  void applyFixedVersion_ShouldCreateTheNewVersionGradeItAndSendTheCaseBackToDraft() {
    givenAPendingRoundOfAnExistingCase();
    var scores = List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build());
    var rq = fixedVersion(scores);

    sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID, rq);

    verify(versionService).createNewDefaultVersion(PROJECT_ID, testCase, rq.getManualScenario(), 52L);
    verify(qualityService).applyQualityScores(PROJECT_ID, newVersion, scores);
    verify(qualityService).applyGenerationMetadata(newVersion, rq.getGeneration());
    assertEquals(TmsTestCaseStatus.DRAFT, testCase.getStatus());
    assertTrue(testCase.isModifiedByAgent());
    assertEquals("New title", testCase.getName());
    assertEquals(TmsFixPushStatus.FIXED, push.getStatus());
    assertEquals(52L, push.getResultIterationId());
    assertNotNull(push.getFinishedAt());
  }

  @Test
  void applyFixedVersion_ShouldMarkEachCommentWithItsAnswerAndTheRound() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID,
        fixedVersion(List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build())));

    assertEquals(TmsReviewCommentState.ADDRESSED, applied.getState());
    assertEquals(1, applied.getRoundNumber());
    assertNull(applied.getAgentReason());
    assertEquals(TmsReviewCommentState.NOT_ADDRESSED, notApplied.getState());
    assertEquals("Needs a new case", notApplied.getAgentReason());
    assertEquals(TmsReviewCommentState.NOT_ADDRESSED, unanswered.getState());
    assertEquals(TmsTestCaseFixRoundService.UNANSWERED_REASON, unanswered.getAgentReason());
  }

  @Test
  void applyFixedVersion_WithAGrade_ShouldRunAutoReadyWithTheThresholdOfThePipeline() {
    givenAPendingRoundOfAnExistingCase();
    givenAutoReady(90);

    sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID,
        fixedVersion(List.of(TmsTestCaseQualityScoreRQ.builder().criterionKey("atomicity").score(15).build())));

    verify(testCaseService).applyAutoReady(PROJECT_ID, TEST_CASE_ID, 90);
  }

  @Test
  void applyFixedVersion_WithoutAGrade_ShouldEndAsFixedGradeFailedAndNotRunAutoReady() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID, fixedVersion(null));

    assertEquals(TmsFixPushStatus.FIXED_GRADE_FAILED, push.getStatus());
    assertEquals(TmsTestCaseStatus.DRAFT, testCase.getStatus());
    verify(testCaseService, never()).applyAutoReady(anyLong(), anyLong(), anyInt());
  }

  @Test
  void applyFixedVersion_ShouldTellTheReviewOfTheIterationsThatTheLifecycleChanged() {
    givenAPendingRoundOfAnExistingCase();

    sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID, fixedVersion(null));

    var event = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertEquals(new TmsTestCasesLifecycleChangedEvent(List.of(TEST_CASE_ID)), event.getValue());
  }

  @Test
  void applyFixedVersion_WhenTheRoundHasAlreadyEnded_ShouldBeAConflictAndChangeNothing() {
    push.setStatus(TmsFixPushStatus.FAILED);
    when(fixPushRepository.findByIdForUpdate(PUSH_ID)).thenReturn(Optional.of(push));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID, fixedVersion(null)));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
    verify(versionService, never()).createNewDefaultVersion(anyLong(), any(), any(), any());
  }

  @Test
  void applyFixedVersion_ForARoundOfAnotherTestCase_ShouldBeNotFound() {
    push.setTestCaseId(999L);
    when(fixPushRepository.findByIdForUpdate(PUSH_ID)).thenReturn(Optional.of(push));

    var ex = assertThrows(ReportPortalException.class,
        () -> sut.applyFixedVersion(PROJECT_ID, TEST_CASE_ID, fixedVersion(null)));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void failRound_ShouldReturnTheCommentsToNotSentAndEndTheRoundAsFailed() {
    when(fixPushRepository.findByIdForUpdate(PUSH_ID)).thenReturn(Optional.of(push));
    when(commentRepository.findByFixPushId(PUSH_ID)).thenReturn(List.of(applied, notApplied));

    sut.failRound(PUSH_ID, "Stage 'Fix' failed");

    assertEquals(TmsFixPushStatus.FAILED, push.getStatus());
    assertEquals("Stage 'Fix' failed", push.getFailureReason());
    assertNotNull(push.getFinishedAt());
    assertEquals(TmsReviewCommentState.NOT_SENT, applied.getState());
    assertNull(applied.getFixPushId());
    assertEquals(TmsReviewCommentState.NOT_SENT, notApplied.getState());
  }

  @Test
  void failRound_WhenTheRoundAlreadyEnded_ShouldDoNothing() {
    push.setStatus(TmsFixPushStatus.FIXED);
    when(fixPushRepository.findByIdForUpdate(PUSH_ID)).thenReturn(Optional.of(push));

    sut.failRound(PUSH_ID, "late");

    assertEquals(TmsFixPushStatus.FIXED, push.getStatus());
    verify(commentRepository, never()).save(any());
  }
}
