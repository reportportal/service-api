package com.epam.reportportal.base.core.tms.sync;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TmsSyncJobExecutor implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TmsSyncJobExecutor.class);

    public static final String JOB_ID_PARAM = "jobId";

    @Autowired
    private TmsSyncJobService tmsSyncJobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var jobId = extractJobId(context);
        if (jobId == null) {
            LOGGER.error("TMS Sync Job execution failed: jobId is missing or invalid in JobDataMap");
            return;
        }

        LOGGER.info("Executing TMS Sync Job via Quartz: {}", jobId);
        try {
            tmsSyncJobService.executeSync(jobId);
        } catch (Exception e) {
            LOGGER.error("TMS Sync Job {} failed during execution", jobId, e);
            throw new JobExecutionException("TMS Sync Job failed", e);
        }
    }

    private Long extractJobId(JobExecutionContext context) {
        if (context == null || context.getMergedJobDataMap() == null) {
            return null;
        }

        var rawJobId = context.getMergedJobDataMap().get(JOB_ID_PARAM);
        if (rawJobId == null) {
            return null;
        }

        if (rawJobId instanceof Number) {
            return ((Number) rawJobId).longValue();
        } else if (rawJobId instanceof String) {
            try {
                return Long.valueOf((String) rawJobId);
            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse jobId parameter '{}' to Long", rawJobId, e);
                return null;
            }
        }

        LOGGER.error("Unexpected type for jobId parameter: {}", rawJobId.getClass().getName());
        return null;
    }
}
