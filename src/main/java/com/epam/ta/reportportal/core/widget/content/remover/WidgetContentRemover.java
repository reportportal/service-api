package com.epam.ta.reportportal.core.widget.content.remover;

import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetContentRemover {

	void removeContent(Widget widget);

	boolean supports(Widget widget);
}
