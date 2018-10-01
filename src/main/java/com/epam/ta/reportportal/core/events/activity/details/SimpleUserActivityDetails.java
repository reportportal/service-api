package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleUserActivityDetails extends JsonbObject {
	private Long userId;

	public SimpleUserActivityDetails(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
