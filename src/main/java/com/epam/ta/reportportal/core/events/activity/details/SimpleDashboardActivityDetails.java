package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleDashboardActivityDetails extends JsonbObject {
	private Long dashboardId;

	public SimpleDashboardActivityDetails(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}
}
