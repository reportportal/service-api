package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.connector.CiRunRequest;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixPush;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
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
class TmsTestCasePushToAgentServiceTest {

  private static final long PROJECT_ID = 1L;
  private static final Long TEST_CASE_ID = 103L;
  private static final Long USER_ID = 5L;
  private static final Long ITERATION_ID = 41L;

  @Mock
  private TmsTestCaseRepository testCaseRepository;
  @Mock
  private TmsTestCaseReviewCommentRepository commentRepository;
  @Mock
  private TmsTestCaseFixPushRepository fixPushRepository;
  @Mock
  private PipelineStageTestCaseRepository stageTestCaseRepository;
  @Mock
  private PipelineIterationRepository iterationRepository;
  @Mock
  private PipelineStageRepository stageRepository;
  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private PlatformTransactionManager transactionManager;
  @Mock
  private CiTriggerConnector githubConnector;

  private TmsTestCasePushToAgentService sut;

  private TmsTestCase aiCase;
  private TmsTestCaseReviewComment unsent;
  private Integration integration;

  @BeforeEach
  void setUp() {
    lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    when(githubConnector.getSupportedExecutorType()).thenReturn("GITHUB_ACTIONS");
    sut = new TmsTestCasePushToAgentService(testCaseRepository, commentRepository, fixPushRepository,
        stageTestCaseRepository, iterationRepository, stageRepository, integrationRepository,
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
    when(githubConnector.getIntegrationTypeName()).thenReturn("github-actions");
    when(integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION)).thenReturn(List.of(integration));
  }

  private void givenTheCaseHasUnsentComments() {
    when(commentRepository.findByTestCaseIdAndState(TEST_CASE_ID, TmsReviewCommentState.NOT_SENT)).thenReturn(List.of(unsent));
  }

  private void givenPushIsSaved() {
    when(fixPushRepository.findLastRoundNumber(TEST_CASE_ID)).thenReturn(0);
    when(fixPushRepository.saveAndFlush(any(TmsTestCaseFixPush.class))).thenAnswer(call -> {
      TmsTestCaseFixPush saved = call.getArgument(0);
      saved.setId(7L);
      return saved;
    });
  }

  @Test
  void push_ShouldRecordRoundOneStartTheJobWithOnlyIdsAndMarkTheCommentsAsSent() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenPushIsSaved();
    var saved = new TmsTestCaseFixPush();
    saved.setId(7L);
    saved.setTestCaseId(TEST_CASE_ID);
    saved.setRoundNumber(1);
    saved.setStatus(TmsFixPushStatus.PENDING);
    when(fixPushRepository.findById(7L)).thenReturn(Optional.of(saved));
    when(githubConnector.triggerRun(any(), any())).thenReturn("https://github.com/org/repo/actions/workflows/fix.yml");
    when(commentRepository.findByFixPushId(7L)).thenReturn(List.of(unsent));

    var round = sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID);

    var request = ArgumentCaptor.forClass(CiRunRequest.class);
    verify(githubConnector).triggerRun(any(), request.capture());
    assertEquals("org/repo", request.getValue().repo());
    assertEquals("ai-pipeline.yml", request.getValue().workflowRef());
    assertEquals(Map.of("test_case_id", "103", "push_id", "7", "previous_iteration_id", "41"), request.getValue().inputs());
    assertEquals(TmsReviewCommentState.SENT, unsent.getState());
    assertEquals(7L, unsent.getFixPushId());
    assertEquals(7L, round.getId());
    assertEquals(1, round.getRoundNumber());
    assertEquals("https://github.com/org/repo/actions/workflows/fix.yml", round.getWorkflowUrl());
  }

  @Test
  void push_ShouldNumberTheRoundAfterTheLastOneOfTheCase() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    when(fixPushRepository.findLastRoundNumber(TEST_CASE_ID)).thenReturn(2);
    when(fixPushRepository.saveAndFlush(any(TmsTestCaseFixPush.class))).thenAnswer(call -> {
      TmsTestCaseFixPush saved = call.getArgument(0);
      saved.setId(9L);
      return saved;
    });
    when(githubConnector.triggerRun(any(), any())).thenReturn("url");
    when(fixPushRepository.findById(9L)).thenAnswer(call -> Optional.of(new TmsTestCaseFixPush()));
    when(commentRepository.findByFixPushId(9L)).thenReturn(List.of());

    sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID);

    var push = ArgumentCaptor.forClass(TmsTestCaseFixPush.class);
    verify(fixPushRepository).saveAndFlush(push.capture());
    assertEquals(3, push.getValue().getRoundNumber());
    assertEquals(TmsFixPushStatus.PENDING, push.getValue().getStatus());
    assertEquals(ITERATION_ID, push.getValue().getPreviousIterationId());
    assertEquals(USER_ID, push.getValue().getRequestedBy());
  }

  @Test
  void push_WhenTheJobCannotBeStarted_ShouldRecordNoRoundAndKeepTheCommentsNotSent() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    givenPushIsSaved();
    when(githubConnector.triggerRun(any(), any())).thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "CI is down"));
    when(commentRepository.findByFixPushId(7L)).thenReturn(List.of(unsent));
    unsent.setState(TmsReviewCommentState.SENT);
    unsent.setFixPushId(7L);

    assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(TmsReviewCommentState.NOT_SENT, unsent.getState());
    assertNull(unsent.getFixPushId());
    verify(fixPushRepository).deleteById(7L);
  }

  @Test
  void push_WithoutUnsentComments_ShouldBeRejectedBeforeTouchingCi() {
    when(commentRepository.findByTestCaseIdAndState(TEST_CASE_ID, TmsReviewCommentState.NOT_SENT)).thenReturn(List.of());

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    verify(githubConnector, never()).triggerRun(any(), any());
  }

  @Test
  void push_OfAManualCase_ShouldBeRejected() {
    aiCase.setOrigin(TmsTestCaseOrigin.MANUAL);

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void push_WhileARoundIsRunning_ShouldBeAConflict() {
    when(fixPushRepository.existsByTestCaseIdAndStatus(TEST_CASE_ID, TmsFixPushStatus.PENDING)).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void push_OfACaseNoIterationProduced_ShouldBeRejected() {
    givenTheCaseHasUnsentComments();
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(TEST_CASE_ID))).thenReturn(List.of());

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void push_WhenNoStageTellsWhereTheIterationRan_ShouldBeRejectedAsNotConnectedToCi() {
    givenTheCaseHasUnsentComments();
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(TEST_CASE_ID))).thenReturn(List.of(ITERATION_ID));
    when(stageRepository.findByIterationIdOrderBySequenceAsc(ITERATION_ID)).thenReturn(List.of(new PipelineStage()));

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertTrue(ex.getMessage().contains("not connected to CI"));
  }

  @Test
  void push_WithoutAnEnabledIntegration_ShouldBeRejected() {
    givenTheCaseHasUnsentComments();
    givenAnIterationWithCiAndAnIntegration();
    integration.setEnabled(false);

    var ex = assertThrows(ReportPortalException.class, () -> sut.push(PROJECT_ID, TEST_CASE_ID, USER_ID));

    assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    verify(fixPushRepository, never()).saveAndFlush(any());
  }

  @Test
  void getPush_OfAnotherTestCase_ShouldBeNotFound() {
    var push = new TmsTestCaseFixPush();
    push.setProjectId(PROJECT_ID);
    push.setTestCaseId(999L);
    when(fixPushRepository.findById(7L)).thenReturn(Optional.of(push));

    var ex = assertThrows(ReportPortalException.class, () -> sut.getPush(PROJECT_ID, TEST_CASE_ID, 7L));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }
}
