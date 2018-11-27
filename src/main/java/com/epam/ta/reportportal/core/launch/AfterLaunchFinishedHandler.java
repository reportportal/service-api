package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.entity.launch.Launch;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface AfterLaunchFinishedHandler {

	void handleRetriesStatistics(Launch launch);
}
