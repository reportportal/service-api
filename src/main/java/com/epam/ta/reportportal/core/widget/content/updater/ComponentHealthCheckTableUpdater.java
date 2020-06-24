package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class ComponentHealthCheckTableUpdater implements WidgetUpdater {

	public static final String STATE = "state";

	@Override
	public void update(Widget widget) {
		if (supports(widget)) {
			new WidgetBuilder(widget).addOption(STATE, WidgetState.CREATED.getValue());
		}
	}

	@Override
	public boolean supports(Widget widget) {
		return WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType().equalsIgnoreCase(widget.getWidgetType());
	}
}
