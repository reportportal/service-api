package com.epam.reportportal.base.core.tms.scheduled;

import com.epam.reportportal.base.core.tms.service.TmsAttachmentService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job for cleaning up expired TMS attachments. * * This job runs periodically to remove TMS attachments that
 * have exceeded their TTL * and are not permanently associated with test cases. It helps maintain storage * efficiency
 * by removing temporary files that users uploaded but never used.
 */
@Service
public class TmsAttachmentCleanupJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(TmsAttachmentCleanupJob.class);

  @Autowired
  private TmsAttachmentService tmsAttachmentService;

  @Override
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOGGER.info("TMS attachment cleanup job has been started");

    try {
      tmsAttachmentService.cleanupExpiredAttachments();
      tmsAttachmentService.setExpirationForUnusedAttachments();
      LOGGER.info("TMS attachment cleanup job completed successfully");
    } catch (Exception e) {
      LOGGER.error("TMS attachment cleanup job failed: {}", e.getMessage(), e);
      throw new JobExecutionException("TMS attachment cleanup failed", e);
    }
  }
}
