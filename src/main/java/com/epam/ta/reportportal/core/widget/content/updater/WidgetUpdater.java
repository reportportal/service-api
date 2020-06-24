package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetUpdater {

	void update(Widget widget);

	boolean supports(Widget widget);
}
