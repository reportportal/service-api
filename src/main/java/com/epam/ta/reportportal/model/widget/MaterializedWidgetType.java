package com.epam.ta.reportportal.model.widget;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public enum MaterializedWidgetType {

	COMPONENT_HEALTH_CHECK_TABLE("componentHealthCheckTable"),
	CUMULATIVE_TREND_CHART("cumulative");

	private final String type;

	MaterializedWidgetType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}
