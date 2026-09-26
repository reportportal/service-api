package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineIterationRefresher;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class RefreshPipelineIterationsOnTestCaseChangeTest {

  private final PipelineStageTestCaseRepository stageTestCaseRepository =
      Mockito.mock(PipelineStageTestCaseRepository.class);
  private final PipelineIterationRefresher refresher = Mockito.mock(PipelineIterationRefresher.class);
  private final RefreshPipelineIterationsOnTestCaseChange listener =
      new RefreshPipelineIterationsOnTestCaseChange(stageTestCaseRepository, refresher);

  @Test
  void onLifecycleChanged_WithoutATransaction_ShouldRefreshTheLinkedIterationsAtOnce() {
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(Set.of(5L))).thenReturn(List.of(7L));

    listener.onLifecycleChanged(new TmsTestCasesLifecycleChangedEvent(List.of(5L)));

    verify(refresher).refreshIterations(List.of(7L));
  }

  @Test
  void onLifecycleChanged_InsideATransaction_ShouldRefreshOnlyAfterTheCommit() {
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(Set.of(5L))).thenReturn(List.of(7L));
    TransactionSynchronizationManager.initSynchronization();
    try {
      listener.onLifecycleChanged(new TmsTestCasesLifecycleChangedEvent(List.of(5L)));

      verify(refresher, never()).refreshIterations(any());
      TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
      verify(refresher).refreshIterations(List.of(7L));
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void onLifecycleChanged_WhenNoIterationIsLinked_ShouldDoNothing() {
    when(stageTestCaseRepository.findIterationIdsByTestCaseIdIn(Set.of(5L))).thenReturn(List.of());

    listener.onLifecycleChanged(new TmsTestCasesLifecycleChangedEvent(List.of(5L)));

    verify(refresher, never()).refreshIterations(any());
  }

  @Test
  void onLifecycleChanged_WithNoTestCases_ShouldNotEvenLookUp() {
    listener.onLifecycleChanged(new TmsTestCasesLifecycleChangedEvent(List.of()));

    verify(stageTestCaseRepository, never()).findIterationIdsByTestCaseIdIn(any());
  }
}
