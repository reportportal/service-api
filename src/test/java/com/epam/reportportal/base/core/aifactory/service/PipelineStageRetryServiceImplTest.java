package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.event.PipelineStageRetryRequestedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Covers the transactional-boundary split: the external CI call must happen between the two DB
 * transactions, not inside one wrapping both.
 */
@ExtendWith(MockitoExtension.class)
class PipelineStageRetryServiceImplTest {

  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private PipelineStageAttemptRepository pipelineStageAttemptRepository;
  @Mock
  private PipelineIterationRefresher iterationRefresher;
  @Mock
  private PipelineCiConnectionService ciConnectionService;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private PlatformTransactionManager transactionManager;
  @Mock
  private CiTriggerConnector githubConnector;

  private PipelineStageRetryServiceImpl sut;

  private PipelineStage stage;
  private Integration integration;

  @BeforeEach
  void setUp() {
    when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    when(githubConnector.getSupportedExecutorType()).thenReturn("GITHUB_ACTIONS");

    sut = new PipelineStageRetryServiceImpl(pipelineStageRepository, pipelineStageAttemptRepository,
        iterationRefresher, ciConnectionService, eventPublisher, List.of(githubConnector), transactionManager);

    var project = new Project();
    project.setId(1L);

    var pipeline = new Pipeline();
    pipeline.setId(2L);
    pipeline.setProject(project);

    var iteration = new PipelineIteration();
    iteration.setId(3L);
    iteration.setPipeline(pipeline);

    stage = new PipelineStage();
    stage.setId(4L);
    stage.setIteration(iteration);
    stage.setStatus("Needs human");
    stage.setStatusCategory(StatusCategory.WAITING);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITHUB_ACTIONS").retryable(true).build()));

    var integrationType = new IntegrationType();
    integrationType.setName("github-actions");
    integration = new Integration();
    integration.setEnabled(true);
    integration.setType(integrationType);
  }

  @Test
  void retryStage_WhenRetryable_ShouldTriggerConnectorThenPersistPendingStatus() {
    stubClaim();
    when(githubConnector.triggerStageRerun(integration, stage)).thenReturn("https://ci/runs/1");

    var result = sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, PipelineStageRetryRQ.builder().comment("retry").build());

    assertEquals(4L, result.getStageId());
    assertEquals("Pending", result.getStatus());
    assertEquals(StatusCategory.PENDING, result.getStatusCategory());
    assertEquals("https://ci/runs/1", result.getTriggeredRunUrl());
    assertEquals("Pending", stage.getStatus());
    assertEquals(StatusCategory.PENDING, stage.getStatusCategory());
    assertEquals(9L, stage.getLastRetriedBy());
    verify(eventPublisher).publishEvent(any(PipelineStageRetryRequestedEvent.class));
  }

  @Test
  void retryStage_WhenStageWasFailed_ShouldRefreshTheIterationStatus() {
    stage.setStatus("Failed");
    stage.setStatusCategory(StatusCategory.FAILED);
    stubClaim();
    when(githubConnector.triggerStageRerun(integration, stage)).thenReturn("https://ci/runs/1");

    sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null);

    verify(iterationRefresher).refresh(stage.getIteration());
  }

  @Test
  void retryStage_WhenStagePassedOrSkipped_ShouldThrowBadRequestWithoutTriggeringConnector() {
    for (var category : List.of(StatusCategory.PASSED, StatusCategory.SKIPPED)) {
      stage.setStatusCategory(category);
      when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));

      var ex = assertThrows(ReportPortalException.class,
          () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

      assertEquals(ErrorType.BAD_REQUEST_ERROR.getCode(), ex.getErrorType().getCode());
    }
    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
  }

  @Test
  void retryStage_WhenStageAlreadyPendingOrRunning_ShouldThrowConflictWithoutTriggeringConnector() {
    for (var category : List.of(StatusCategory.PENDING, StatusCategory.RUNNING)) {
      stage.setStatusCategory(category);
      when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));

      var ex = assertThrows(ReportPortalException.class,
          () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

      assertEquals(ErrorType.PIPELINE_STAGE_RETRY_CONFLICT.getCode(), ex.getErrorType().getCode());
    }
    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void retryStage_WhenStatusUnknownOrStageMaintainedByTheSystem_ShouldNotBeRetryable() {
    stage.setStatusCategory(StatusCategory.UNKNOWN);
    when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));
    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    stage.setStatusCategory(StatusCategory.FAILED);
    stage.setOwnedBy(StageOwner.REPORTPORTAL);
    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(githubConnector, never()).triggerStageRerun(any(), any());
  }

  @Test
  void retryStage_WhenConnectorFails_ShouldRestoreStageStatusAndRefreshTheIteration() {
    stage.setStatus("Failed");
    stage.setStatusCategory(StatusCategory.FAILED);
    stubClaim();
    when(githubConnector.triggerStageRerun(integration, stage)).thenThrow(new IllegalStateException("ci down"));

    assertThrows(IllegalStateException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    assertEquals("Failed", stage.getStatus());
    assertEquals(StatusCategory.FAILED, stage.getStatusCategory());
    verify(iterationRefresher, org.mockito.Mockito.times(2)).refresh(stage.getIteration());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void retryStage_WhenFailed_ShouldArchiveFinishedAttemptAndStartANewOne() {
    stage.setStatus("Failed");
    stage.setStatusCategory(StatusCategory.FAILED);
    stage.setStageKey("upload");
    stage.setAttempt(1);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITHUB_ACTIONS").externalRunId("job-7").retryable(true).build()));
    stage.setStartedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
    stage.setFinishedAt(java.time.Instant.parse("2026-01-01T10:01:00Z"));
    stubClaim();
    when(githubConnector.triggerStageRerun(integration, stage)).thenReturn("https://ci/runs/1");

    sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null);

    var archived = org.mockito.ArgumentCaptor.forClass(PipelineStageAttempt.class);
    verify(pipelineStageAttemptRepository).save(archived.capture());
    assertEquals(1, archived.getValue().getAttempt());
    assertEquals("Failed", archived.getValue().getStatus());
    assertEquals(StatusCategory.FAILED, archived.getValue().getStatusCategory());
    assertEquals("upload", archived.getValue().getStageKey());
    assertEquals("job-7", archived.getValue().getExecution().getContent().getExternalRunId());
    assertEquals(2, stage.getAttempt());
    assertNull(stage.getStartedAt());
    assertNull(stage.getFinishedAt());
  }

  @Test
  void retryStage_WhenConnectorFails_ShouldNotArchiveOrBumpAttempt() {
    stage.setStatus("Failed");
    stage.setStatusCategory(StatusCategory.FAILED);
    stubClaim();
    when(githubConnector.triggerStageRerun(integration, stage)).thenThrow(new IllegalStateException("ci down"));

    assertThrows(IllegalStateException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(pipelineStageAttemptRepository, never()).save(any());
    assertEquals(1, stage.getAttempt());
  }

  @Test
  void retryStage_WhenNotRetryable_ShouldThrowBadRequestWithoutTriggeringConnectorOrSaving() {
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITHUB_ACTIONS").retryable(false).build()));
    when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));

    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void retryStage_WhenNoEnabledIntegration_ShouldThrowBadRequestWithoutTriggeringConnector() {
    when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));
    when(ciConnectionService.integrationFor(any(), any()))
        .thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "No enabled integration"));

    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
  }

  @Test
  void retryStage_WhenThePipelineIsNotConnected_ShouldRefuseBeforeTouchingTheStage() {
    when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));
    doThrow(new ReportPortalException(ErrorType.CI_NOT_CONNECTED, "CONNECTION_FAILED"))
        .when(ciConnectionService).assertCanStartJobs(any());

    var thrown = assertThrows(ReportPortalException.class,
        () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    assertEquals(ErrorType.CI_NOT_CONNECTED.getCode(), thrown.getErrorType().getCode());
    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
  }

  @Test
  void retryStage_WhenTheCiAcceptsTheStart_ShouldRecordItAsProofOfTheConnection() {
    stubClaim();
    when(githubConnector.triggerStageRerun(any(), any())).thenReturn("https://ci/run/1");

    sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null);

    verify(ciConnectionService).recordJobStart(2L, null);
  }

  @Test
  void retryStage_WhenTheCiRefusesTheStart_ShouldRecordTheFailureAndRethrow() {
    stubClaim();
    when(githubConnector.triggerStageRerun(any(), any()))
        .thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "HTTP code: 404"));

    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(ciConnectionService).recordJobStart(eq(2L), contains("404"));
  }

  private void stubClaim() {
    when(pipelineStageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(stage));
    lenient().when(pipelineStageRepository.findById(4L)).thenReturn(Optional.of(stage));
    when(ciConnectionService.integrationFor(any(), any())).thenReturn(integration);
  }
}
