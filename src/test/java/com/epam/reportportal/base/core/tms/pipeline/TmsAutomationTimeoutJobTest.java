package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationRefresher;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsAutomationTimeoutJobTest {

  private static final Instant NOW = Instant.parse("2026-11-24T10:30:00Z");

  @Mock
  private PipelineIterationRepository iterationRepository;
  @Mock
  private PipelineStageRepository stageRepository;
  @Mock
  private PipelineIterationRefresher refresher;

  private TmsAutomationTimeoutJob job;

  @BeforeEach
  void setUp() {
    job = new TmsAutomationTimeoutJob(iterationRepository, stageRepository, refresher, Duration.ofMinutes(30));
  }

  private PipelineIteration pendingIteration() {
    var iteration = new PipelineIteration();
    iteration.setId(75L);
    iteration.setStatusCategory(StatusCategory.PENDING);
    iteration.setStartedAt(Instant.parse("2026-11-24T10:00:00Z"));
    when(iterationRepository.findByPipelineTypeAndStatusCategoryAndCreatedAtBefore(
        TmsAutomationStartService.PIPELINE_TYPE, StatusCategory.PENDING, Instant.parse("2026-11-24T10:00:00Z")))
        .thenReturn(List.of(iteration));
    when(iterationRepository.findByIdForUpdate(75L)).thenReturn(Optional.of(iteration));
    return iteration;
  }

  @Test
  void failSilentRuns_WhenTheJobNeverReported_ShouldEndTheIterationAsTimedOut() {
    var iteration = pendingIteration();
    when(stageRepository.findByIterationIdOrderBySequenceAsc(75L)).thenReturn(List.of());
    when(stageRepository.save(any(PipelineStage.class))).thenAnswer(invocation -> {
      PipelineStage stage = invocation.getArgument(0);
      stage.setId(900L);
      return stage;
    });

    var ended = job.failSilentRuns(NOW);

    assertEquals(1, ended);
    var saved = ArgumentCaptor.forClass(PipelineStage.class);
    verify(stageRepository, org.mockito.Mockito.times(2)).save(saved.capture());
    var stage = saved.getAllValues().get(1);
    assertEquals(StatusCategory.TIMED_OUT, stage.getStatusCategory());
    assertEquals("The CI job did not report in 30 minutes", stage.getMessage());
    assertEquals("900", stage.getPath());
    assertEquals(NOW, iteration.getFinishedAt());
    verify(refresher).refresh(iteration);
  }

  @Test
  void failSilentRuns_WhenAStageArrivedMeanwhile_ShouldLeaveTheIterationAlone() {
    pendingIteration();
    when(stageRepository.findByIterationIdOrderBySequenceAsc(75L)).thenReturn(List.of(new PipelineStage()));

    var ended = job.failSilentRuns(NOW);

    assertEquals(0, ended);
    verify(refresher, never()).refresh(any());
  }
}
