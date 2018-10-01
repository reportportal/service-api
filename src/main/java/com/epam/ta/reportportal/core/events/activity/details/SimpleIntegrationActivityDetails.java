package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleIntegrationActivityDetails extends JsonbObject {
	private Long integrationId;

	public SimpleIntegrationActivityDetails(Long integrationId) {
		this.integrationId = integrationId;
	}

	public Long getIntegrationId() {
		return integrationId;
	}

	public void setIntegrationId(Long integrationId) {
		this.integrationId = integrationId;
	}
}
