package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class MaterializedWidgetStateUpdater implements WidgetUpdater {

	public static final String STATE = "state";

	@Override
	public void update(Widget widget) {
		new WidgetBuilder(widget).addOption(STATE, WidgetState.CREATED.getValue());
	}
}
