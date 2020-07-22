package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.entity.widget.Widget;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface MaterializedLoadContentStrategy {

	Map<String, Object> loadContent(Widget widget, String[] attributes, Map<String, String> params);
}
