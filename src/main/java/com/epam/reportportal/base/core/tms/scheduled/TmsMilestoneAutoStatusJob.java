package com.epam.reportportal.base.core.tms.scheduled;

import com.epam.reportportal.base.core.tms.service.TmsMilestoneAutoStatusService;
import java.time.Instant;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Scheduled Quartz job for the automatic milestone status: moves Scheduled milestones to Testing on
 * their start date or after their first execution, and Testing milestones to Completed when their
 * deadline day is over, according to the options stored on each milestone.
 */
@Service
@DisallowConcurrentExecution
public class TmsMilestoneAutoStatusJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(TmsMilestoneAutoStatusJob.class);

  @Autowired
  private TmsMilestoneAutoStatusService tmsMilestoneAutoStatusService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      tmsMilestoneAutoStatusService.applyToAll(Instant.now());
    } catch (Exception e) {
      LOGGER.error("TMS milestone automatic status job failed: {}", e.getMessage(), e);
      throw new JobExecutionException("TMS milestone automatic status failed", e);
    }
  }
}
