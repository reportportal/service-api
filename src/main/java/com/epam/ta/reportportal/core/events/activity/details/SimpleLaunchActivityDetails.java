package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleLaunchActivityDetails extends JsonbObject {
	private Long launchId;

	public SimpleLaunchActivityDetails(Long launchId) {
		this.launchId = launchId;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}
}
