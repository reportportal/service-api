package com.epam.reportportal.base.core.tms.scheduled;

import com.epam.reportportal.base.core.tms.sync.TmsSyncJobService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Scheduled Quartz job for Transactional Outbox polling and stale sync job recovery.
 * Picks up any PENDING jobs that missed immediate Quartz triggering, and marks dead/stale IN_PROGRESS jobs as FAILED.
 */
@Service
public class TmsSyncOutboxPollerJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(TmsSyncOutboxPollerJob.class);
  private static final int POLL_BATCH_SIZE = 10;
  private static final long STALE_JOB_THRESHOLD_HOURS = 2L;

  @Autowired
  private TmsSyncJobRepository tmsSyncJobRepository;

  @Autowired
  private TmsSyncJobService tmsSyncJobService;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    var transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    pollPendingJobs(transactionTemplate);
    recoverStaleJobs(transactionTemplate);
  }

  private void pollPendingJobs(TransactionTemplate transactionTemplate) {
    var pendingJobIds = transactionTemplate.execute(status ->
        tmsSyncJobRepository.findPendingJobIdsForExecution(POLL_BATCH_SIZE)
    );

    if (pendingJobIds == null || pendingJobIds.isEmpty()) {
      return;
    }

    LOGGER.info("TMS Outbox Poller found {} pending sync job(s) for execution: {}",
        pendingJobIds.size(), pendingJobIds);

    for (Long jobId : pendingJobIds) {
      try {
        tmsSyncJobService.executeSync(jobId);
      } catch (Exception e) {
        LOGGER.error("TMS Outbox Poller execution failed for sync job: {}", jobId, e);
      }
    }
  }

  private void recoverStaleJobs(TransactionTemplate transactionTemplate) {
    var staleThreshold = Instant.now().minus(STALE_JOB_THRESHOLD_HOURS, ChronoUnit.HOURS);
    var completedAt = Instant.now();

    var affectedCount = transactionTemplate.execute(status ->
        tmsSyncJobRepository.markStaleInProgressJobsAsFailed(staleThreshold, completedAt)
    );

    if (affectedCount != null && affectedCount > 0) {
      LOGGER.warn("TMS Outbox Poller marked {} stale IN_PROGRESS sync job(s) as FAILED", affectedCount);
    }
  }
}
