package com.epam.reportportal.base.core.tms.sync;

import com.epam.reportportal.base.core.tms.sync.event.TmsSyncJobScheduledEvent;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles scheduling of TMS sync jobs after transaction commit.
 * Uses @TransactionalEventListener to ensure scheduling happens only after successful transaction commit.
 */
@Slf4j
@Component
public class TmsSyncJobScheduler {

  private final Scheduler scheduler;

  public TmsSyncJobScheduler(ObjectProvider<Scheduler> schedulerProvider) {
    this.scheduler = schedulerProvider.getIfAvailable();
  }

  /**
   * Schedules a TMS sync job after the transaction commits.
   * This method is triggered by {@link TmsSyncJobScheduledEvent} only after successful transaction commit.
   *
   * @param event the TMS sync job scheduled event containing the job ID
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTmsSyncJobScheduled(TmsSyncJobScheduledEvent event) {
    if (scheduler == null) {
      log.warn("Quartz Scheduler is not available for job {}", event.getJobId());
      return;
    }

    try {
      var jobId = event.getJobId();
      var jobDetail = JobBuilder.newJob(TmsSyncJobExecutor.class)
          .withIdentity("tmsSyncJob_" + jobId, "tmsSync")
          .usingJobData(TmsSyncJobExecutor.JOB_ID_PARAM, jobId)
          .build();

      var trigger = TriggerBuilder.newTrigger()
          .withIdentity("tmsSyncTrigger_" + jobId, "tmsSync")
          .startNow()
          .build();

      scheduler.scheduleJob(jobDetail, trigger);
      log.info("Successfully scheduled TMS Sync Job: {}", jobId);
    } catch (SchedulerException e) {
      log.error("Failed to schedule TMS Sync Job {}", event.getJobId(), e);
    }
  }
}