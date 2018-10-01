package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleFilterActivityDetails extends JsonbObject {
	private Long userFilterId;

	public SimpleFilterActivityDetails(Long userFilterId) {
		this.userFilterId = userFilterId;
	}

	public Long getUserFilterId() {
		return userFilterId;
	}

	public void setUserFilterId(Long userFilterId) {
		this.userFilterId = userFilterId;
	}
}
