package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LaunchAccessValidator {

	/**
	 * @param launchId       {@link com.epam.ta.reportportal.entity.launch.Launch#getId()}
	 * @param projectDetails {@link ReportPortalUser.ProjectDetails}
	 * @param user           {@link ReportPortalUser}
	 */
	void validate(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}
