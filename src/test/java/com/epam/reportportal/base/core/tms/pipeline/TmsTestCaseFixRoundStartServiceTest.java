package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.connector.CiRunRequest;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.service.PipelineCiConnectionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseOrigin;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseFixRoundStartServiceTest {

  private static final long PROJECT_ID = 1L;
  private static final Long TEST_CASE_ID = 103L;
  private static final Long USER_ID = 5L;
  private static final Long ITERATION_ID = 41L;

  @Mock
  private TmsTestCaseRepository testCaseRepository;
  @Mock
  private TmsTestCaseReviewCommentRepository commentRepository;
  @Mock
  private TmsTestCaseFixRoundRepository fixRoundRepository;
  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;
  @Mock
  private PipelineIterationRepository iterationRepository;
  @Mock
  private PipelineStageRepository stageRepository;
  @Mock
  private PipelineCiConnectionService ciConnectionService;
  @Mock
  private PlatformTransactionManager transactionManager;
  @Mock
  private CiTriggerConnector githubConnector;

  private TmsTestCaseFixRoundStartService sut;

  private TmsTestCase aiCase;
  private TmsTestCaseReviewComment unsent;
  private Integration integration;

  @BeforeEach
  void setUp() {
    lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    when(githubConnector.getSupportedExecutorType()).thenReturn("GITHUB_ACTIONS");
    sut = new TmsTestCaseFixRoundStartService(testCaseRepository, commentRepository, fixRoundRepository,
        stageTestCaseRepository, iterationRepository, stageRepository, ciConnectionService,
        List.of(githubConnector), transactionManager);

    aiCase = new TmsTestCase();
    aiCase.setId(TEST_CASE_ID);
    aiCase.setOrigin(TmsTestCaseOrigin.AI);
    lenient().when(testCaseRepository.findByProjectIdAndId(PROJECT_ID, TEST_CASE_ID)).thenReturn(Optional.of(aiCase));

    unsent = new TmsTestCaseReviewComment();
    unsent.setId(11L);
    unsent.setTestCaseId(TEST_CASE_ID);
    unsent.setState(TmsReviewCommentState.NOT_SENT);
  }

  private void givenAnIterationWithCiAndAnIntegration() {
    var project = new Project();
    project.setId(PROJECT_ID);
    var pipeline = new Pipeline();
    pipeline.setProject(project);
    var iteration = new PipelineIteration();
    iteration.setId(ITERATION_ID);
    iteration.setPipeline(pipeline);
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(TEST_CASE_ID))).thenReturn(List.of(30L, ITERATION_ID));
    when(iterationRepository.findById(ITERATION_ID)).thenReturn(Optional.of(iteration));

    var stage = new PipelineStage();
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITHUB_ACTIONS")
        .retryParameters(new HashMap<>(Map.of("repo", "org/repo", "workflowRef", "ai-pipeline.yml"))).build()));
    when(stageRepository.findByIterationIdOrderBySequenceAsc(ITERATION_ID)).thenReturn(List.of(new PipelineStage(), stage));

    var type = new IntegrationType();
    type.setName("github-actions");
    integration = new Integration();
    integration.setEnabled(true);
    integration.setType(type);
    lenient().when(ciConnectionService.integrationFor(pipeline, githubConnector)).thenReturn(integration);
    // a connector with nothing to fill in hands the request back
    lenient().when(githubConnector.resolveRun(any(), any())).thenAnswer(call -> call.getArgument(1));
  }

  private void givenTheCaseHasUnsentComments() {
    when(commentRepository.findByTestCaseIdAndState(TEST_CASE_ID, TmsReviewCommentState.NOT_SENT)).thenReturn(List.of(unsent));
  }

  private void givenTheRoundIsSaved() {
    when(fixRoundRepository.findLastRoundNumber(TEST_CASE_ID)).thenReturn(0);
    when(fixRoundRepository.saveAndFlush(any(TmsTestCaseFixRound.class))).thenAnswer(call -> {
      TmsTestCaseFixRound saved = call.getArgument(0);
      saved.setId(7L);
      return saved;
    });
  }

  @Test
  void start_ShouldRecordRoundOneStartTheJobWithOnlyIdsAndMarkTheCommentsAsSent() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenTheRoundIsSaved();
    var saved = new TmsTestCaseFixRound();
    saved.setId(7L);
    saved.setTestCaseId(TEST_CASE_ID);
    saved.setRoundNumber(1);
    saved.setStatus(TmsFixRoundStatus.PENDING);
    when(fixRoundRepository.findById(7L)).thenReturn(Optional.of(saved));
    when(githubConnector.triggerRun(any(), any())).thenReturn("https://github.com/org/repo/actions/workflows/fix.yml");
    when(commentRepository.findByFixRoundId(7L)).thenReturn(List.of(unsent));

    var round = sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID);

    var request = ArgumentCaptor.forClass(CiRunRequest.class);
    verify(githubConnector).triggerRun(any(), request.capture());
    assertEquals("org/repo", request.getValue().repo());
    assertEquals("ai-pipeline.yml", request.getValue().workflowRef());
    assertEquals(Map.of("test_case_id", "103", "fix_round_id", "7", "previous_iteration_id", "41"), request.getValue().inputs());
    assertEquals(TmsReviewCommentState.SENT, unsent.getState());
    assertEquals(7L, unsent.getFixRoundId());
    assertEquals(7L, round.getId());
    assertEquals(1, round.getRoundNumber());
    assertEquals("https://github.com/org/repo/actions/workflows/fix.yml", round.getWorkflowUrl());
  }

  @Test
  void start_ShouldNumberTheRoundAfterTheLastOneOfTheCase() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    when(fixRoundRepository.findLastRoundNumber(TEST_CASE_ID)).thenReturn(2);
    when(fixRoundRepository.saveAndFlush(any(TmsTestCaseFixRound.class))).thenAnswer(call -> {
      TmsTestCaseFixRound saved = call.getArgument(0);
      saved.setId(9L);
      return saved;
    });
    when(githubConnector.triggerRun(any(), any())).thenReturn("url");
    when(fixRoundRepository.findById(9L)).thenAnswer(call -> Optional.of(new TmsTestCaseFixRound()));
    when(commentRepository.findByFixRoundId(9L)).thenReturn(List.of());

    sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID);

    var round = ArgumentCaptor.forClass(TmsTestCaseFixRound.class);
    verify(fixRoundRepository).saveAndFlush(round.capture());
    assertEquals(3, round.getValue().getRoundNumber());
    assertEquals(TmsFixRoundStatus.PENDING, round.getValue().getStatus());
    assertEquals(ITERATION_ID, round.getValue().getPreviousIterationId());
    assertEquals(USER_ID, round.getValue().getRequestedBy());
  }

  @Test
  void start_WhenTheJobCannotBeStarted_ShouldRecordNoRoundAndKeepTheCommentsNotSent() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenTheRoundIsSaved();
    when(githubConnector.triggerRun(any(), any())).thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "CI is down"));
    when(commentRepository.findByFixRoundId(7L)).thenReturn(List.of(unsent));
    unsent.setState(TmsReviewCommentState.SENT);
    unsent.setFixRoundId(7L);

    assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(TmsReviewCommentState.NOT_SENT, unsent.getState());
    assertNull(unsent.getFixRoundId());
    verify(fixRoundRepository).deleteById(7L);
    verify(ciConnectionService).recordJobStart(any(), contains("CI is down"));
  }

  @Test
  void start_WhenThePipelineIsNotConnected_ShouldRefuseBeforeRecordingAnyRound() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    doThrow(new ReportPortalException(ErrorType.CI_NOT_CONNECTED, "CONNECTION_FAILED"))
        .when(ciConnectionService).assertCanStartJobs(any());

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.CI_NOT_CONNECTED.getCode(), ex.getErrorType().getCode());
    verify(fixRoundRepository, never()).saveAndFlush(any());
    verify(githubConnector, never()).triggerRun(any(), any());
    assertEquals(TmsReviewCommentState.NOT_SENT, unsent.getState());
  }

  @Test
  void start_WhenTheCiAcceptsTheJob_ShouldRecordItAsProofOfTheConnection() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenTheRoundIsSaved();
    when(githubConnector.triggerRun(any(), any())).thenReturn("url");
    when(fixRoundRepository.findById(7L)).thenAnswer(call -> Optional.of(new TmsTestCaseFixRound()));
    when(commentRepository.findByFixRoundId(7L)).thenReturn(List.of());

    sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID);

    verify(ciConnectionService).recordJobStart(any(), isNull());
  }

  @Test
  void start_WhenTheRunIsRefusedBeforeStarting_ShouldRecordNoRoundAndLeaveTheCommentsAlone() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    when(githubConnector.resolveRun(any(), any()))
        .thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Wrong CI project"));

    assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    verify(fixRoundRepository, never()).saveAndFlush(any());
    verify(githubConnector, never()).triggerRun(any(), any());
    assertEquals(TmsReviewCommentState.NOT_SENT, unsent.getState());
  }

  @Test
  void start_ShouldStartTheJobWithTheRefTheConnectorResolved() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenTheRoundIsSaved();
    when(githubConnector.resolveRun(any(), any()))
        .thenReturn(new CiRunRequest("org/repo", "release", Map.of()));
    when(githubConnector.triggerRun(any(), any())).thenReturn("url");
    when(fixRoundRepository.findById(7L)).thenAnswer(call -> Optional.of(new TmsTestCaseFixRound()));
    when(commentRepository.findByFixRoundId(7L)).thenReturn(List.of());

    sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID);

    var request = ArgumentCaptor.forClass(CiRunRequest.class);
    verify(githubConnector).triggerRun(any(), request.capture());
    assertEquals("release", request.getValue().workflowRef());
    assertEquals("7", request.getValue().inputs().get("fix_round_id"));
  }

  @Test
  void start_WithoutUnsentComments_ShouldBeRejectedBeforeTouchingCi() {
    when(commentRepository.findByTestCaseIdAndState(TEST_CASE_ID, TmsReviewCommentState.NOT_SENT)).thenReturn(List.of());

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    verify(githubConnector, never()).triggerRun(any(), any());
  }

  @Test
  void start_OfAManualCase_ShouldBeRejected() {
    aiCase.setOrigin(TmsTestCaseOrigin.MANUAL);

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void start_WhileARoundIsRunning_ShouldBeAConflict() {
    when(fixRoundRepository.existsByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixRoundStatus.PENDING)).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void start_OfACaseNoIterationProduced_ShouldBeRejected() {
    givenTheCaseHasUnsentComments();
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(TEST_CASE_ID))).thenReturn(List.of());

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void start_WhenNoStageTellsWhereTheIterationRan_ShouldBeRejectedAsNotConnectedToCi() {
    givenTheCaseHasUnsentComments();
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(TEST_CASE_ID))).thenReturn(List.of(ITERATION_ID));
    when(stageRepository.findByIterationIdOrderBySequenceAsc(ITERATION_ID)).thenReturn(List.of(new PipelineStage()));

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertTrue(ex.getMessage().contains("not connected to CI"));
  }

  @Test
  void start_WithoutAnEnabledIntegration_ShouldBeRejected() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    when(ciConnectionService.integrationFor(any(), any()))
        .thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "No enabled integration"));

    var ex = assertThrows(ReportPortalException.class, () -> sut.start(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    verify(fixRoundRepository, never()).saveAndFlush(any());
  }

  @Test
  void getRounds_ShouldListTheRoundsOfTheCaseLatestFirstWithTheirResultIterationAndWithoutComments() {
    var second = new TmsTestCaseFixRound();
    second.setId(8L);
    second.setRoundNumber(2);
    second.setStatus(TmsFixRoundStatus.PENDING);
    var first = new TmsTestCaseFixRound();
    first.setId(7L);
    first.setRoundNumber(1);
    first.setStatus(TmsFixRoundStatus.FAILED);
    first.setResultIterationId(55L);
    when(fixRoundRepository.findByProjectIdAndTestCaseIdOrderByRoundNumberDesc(PROJECT_ID, TEST_CASE_ID))
        .thenReturn(List.of(second, first));

    var rounds = sut.getRounds(PROJECT_ID, TEST_CASE_ID);

    assertEquals(2, rounds.size());
    assertEquals(2, rounds.get(0).getRoundNumber());
    assertNull(rounds.get(0).getResultIterationId());
    assertEquals(55L, rounds.get(1).getResultIterationId());
    assertNull(rounds.get(0).getComments());
    assertNull(rounds.get(1).getComments());
  }

  @Test
  void getRounds_OfATestCaseThatIsNotInTheProject_ShouldBeNotFound() {
    var ex = assertThrows(ReportPortalException.class, () -> sut.getRounds(PROJECT_ID, 999L));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void getRound_OfAnotherTestCase_ShouldBeNotFound() {
    var round = new TmsTestCaseFixRound();
    round.setProjectId(PROJECT_ID);
    round.setTestCaseId(999L);
    when(fixRoundRepository.findById(7L)).thenReturn(Optional.of(round));

    var ex = assertThrows(ReportPortalException.class, () -> sut.getRound(PROJECT_ID, TEST_CASE_ID, 7L));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }
}
