package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * Interface for widget parameters validation.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetValidator {

	/**
	 * @param widget {@link Widget} to validate
	 */
	void validate(Widget widget);
}
