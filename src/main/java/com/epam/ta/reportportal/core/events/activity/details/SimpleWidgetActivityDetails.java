package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

public class SimpleWidgetActivityDetails extends JsonbObject {
	private Long widgetId;

	public SimpleWidgetActivityDetails(Long widgetId) {
		this.widgetId = widgetId;
	}

	public Long getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Long widgetId) {
		this.widgetId = widgetId;
	}
}
