package com.epam.ta.reportportal.core.events.widget;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GenerateComponentHealthCheckTableEvent {

	private final Long widgetId;
	private final boolean refresh;
	private final List<String> attributeKeys;

	public GenerateComponentHealthCheckTableEvent(Long widgetId, boolean refresh) {
		this.widgetId = widgetId;
		this.refresh = refresh;
		this.attributeKeys = Lists.newArrayList();
	}

	public GenerateComponentHealthCheckTableEvent(Long widgetId, boolean refresh, List<String> attributeKeys) {
		this.widgetId = widgetId;
		this.refresh = refresh;
		this.attributeKeys = attributeKeys;
	}

	public Long getWidgetId() {
		return widgetId;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public List<String> getAttributeKeys() {
		return attributeKeys;
	}
}
