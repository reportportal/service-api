package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineIterationRefresher;
import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Recounts the review progress of a pipeline iteration when one of its test cases changes.
 * <p>
 * Example: iteration #4 produced test cases TC-1..TC-3 and its Review stage shows "1 / 3 Ready". A reviewer
 * marks TC-2 as Ready in the Library; this listener finds iteration #4 through the link between its stage
 * and TC-2 and refreshes it, so the Review stage shows "2 / 3 Ready" without any new report from the
 * pipeline.
 * <p>
 * The iterations are looked up when the event arrives (a deleted case no longer has its link afterwards)
 * and refreshed once the change has been committed.
 */
@Component
@RequiredArgsConstructor
public class RefreshPipelineIterationsOnTestCaseChange {

  private final PipelineStageTestCaseRepository stageTestCaseRepository;
  private final PipelineIterationRefresher refresher;

  @EventListener
  public void onLifecycleChanged(TmsTestCasesLifecycleChangedEvent event) {
    if (event.testCaseIds().isEmpty()) {
      return;
    }
    var iterationIds = stageTestCaseRepository.findIterationIdsByTestCaseIdIn(event.testCaseIds());
    if (iterationIds.isEmpty()) {
      return;
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          refresher.refreshIterations(iterationIds);
        }
      });
    } else {
      refresher.refreshIterations(iterationIds);
    }
  }
}
