package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.entity.project.Project;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LaunchCleanerService {

	void cleanOutdatedLaunches(Project project, Duration period, AtomicLong launchesRemoved);
}
