package com.epam.ta.reportportal.job;

import java.time.Duration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LogCleanerService {

	void removeOutdatedLogs(Long projectId, Duration period);
}
