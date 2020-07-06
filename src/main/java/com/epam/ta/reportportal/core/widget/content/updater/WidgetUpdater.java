package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * Interface for widget parameters update
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetUpdater {

	/**
	 * @param widget {@link Widget} to update
	 */
	void update(Widget widget);

}
