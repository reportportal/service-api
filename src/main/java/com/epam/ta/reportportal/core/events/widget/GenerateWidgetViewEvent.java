package com.epam.ta.reportportal.core.events.widget;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GenerateWidgetViewEvent {

	private final Long widgetId;
	private final MultiValueMap<String, String> params;

	public GenerateWidgetViewEvent(Long widgetId, MultiValueMap<String, String> params) {
		this.widgetId = widgetId;
		this.params = params;
	}

	public GenerateWidgetViewEvent(Long widgetId) {
		this.widgetId = widgetId;
		this.params = new LinkedMultiValueMap<>();
	}

	public Long getWidgetId() {
		return widgetId;
	}

	public MultiValueMap<String, String> getParams() {
		return params;
	}
}
