package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.database.entity.Launch;

public interface IRetriesLaunchHandler {

	void collectRetries(Launch launch);

}
