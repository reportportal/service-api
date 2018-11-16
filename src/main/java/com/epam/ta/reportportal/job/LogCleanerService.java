package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.entity.project.Project;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LogCleanerService {

	void removeOutdatedLogs(Project project, Duration period, AtomicLong removedLogsCount);

	void removeProjectAttachments(Project project, Duration period, AtomicLong removedAttachmentsCount, AtomicLong removedThumbnailsCount);
}
