package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.entity.widget.Widget;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface HealthCheckTableContentResolver {

	Map<String, Object> loadContent(Widget widget, String[] attributes, Map<String, String> params);
}
