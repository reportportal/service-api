package com.epam.ta.reportportal.core.item.impl.status;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.launch.Launch;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ChangeStatusHandler {

	void changeParentStatus(Long childId, Long projectId, ReportPortalUser user);

	void changeLaunchStatus(Launch launch);
}
