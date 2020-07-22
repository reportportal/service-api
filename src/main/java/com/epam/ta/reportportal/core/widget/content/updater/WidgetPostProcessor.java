package com.epam.ta.reportportal.core.widget.content.updater;

import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * Interface for widget post processing after creation
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetPostProcessor {

	/**
	 * @param widget {@link Widget}
	 * @return 'true' if provided widget is supported by post processor otherwise 'false'
	 */
	boolean supports(Widget widget);

	/**
	 * @param widget {@link Widget} for post processing
	 */
	void postProcess(Widget widget);
}
