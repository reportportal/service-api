package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.core.aifactory.event.PipelineStageRetryRequestedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
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
 * Covers the transactional-boundary split from code review round 2, finding #5: the external CI
 * call must happen between the two DB transactions, not inside one wrapping both.
 */
@ExtendWith(MockitoExtension.class)
class PipelineStageRetryServiceImplTest {

  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private IntegrationRepository integrationRepository;
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
    when(githubConnector.getSupportedProvider()).thenReturn(CiProvider.GITHUB_ACTIONS);

    sut = new PipelineStageRetryServiceImpl(pipelineStageRepository, integrationRepository,
        eventPublisher, List.of(githubConnector), transactionManager);

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
    stage.setStatus(PipelineRunStatus.NEEDS_HUMAN);
    stage.setCiProvider(CiProvider.GITHUB_ACTIONS);
    stage.setRetryable(true);

    var integrationType = new IntegrationType();
    integrationType.setName("github-actions");
    integration = new Integration();
    integration.setEnabled(true);
    integration.setType(integrationType);
  }

  @Test
  void retryStage_WhenRetryable_ShouldTriggerConnectorThenPersistPendingStatus() {
    when(pipelineStageRepository.findById(4L)).thenReturn(Optional.of(stage));
    when(integrationRepository.findAllProjectByGroup(any(), any())).thenReturn(List.of(integration));
    when(githubConnector.triggerStageRerun(integration, stage)).thenReturn("https://ci/runs/1");

    var result = sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, PipelineStageRetryRQ.builder().comment("retry").build());

    assertEquals(4L, result.getStageId());
    assertEquals(PipelineRunStatus.PENDING, result.getStatus());
    assertEquals("https://ci/runs/1", result.getTriggeredRunUrl());
    assertEquals(PipelineRunStatus.PENDING, stage.getStatus());
    verify(pipelineStageRepository).save(stage);
    verify(eventPublisher).publishEvent(any(PipelineStageRetryRequestedEvent.class));
  }

  @Test
  void retryStage_WhenNotRetryable_ShouldThrowBadRequestWithoutTriggeringConnectorOrSaving() {
    stage.setRetryable(false);
    when(pipelineStageRepository.findById(4L)).thenReturn(Optional.of(stage));

    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void retryStage_WhenNoEnabledIntegration_ShouldThrowBadRequestWithoutTriggeringConnector() {
    when(pipelineStageRepository.findById(4L)).thenReturn(Optional.of(stage));
    when(integrationRepository.findAllProjectByGroup(any(), any())).thenReturn(List.of());

    assertThrows(ReportPortalException.class, () -> sut.retryPipelineIterationStage(1L, 9L, 3L, 4L, null));

    verify(githubConnector, never()).triggerStageRerun(any(), any());
    verify(pipelineStageRepository, never()).save(any());
  }
}
