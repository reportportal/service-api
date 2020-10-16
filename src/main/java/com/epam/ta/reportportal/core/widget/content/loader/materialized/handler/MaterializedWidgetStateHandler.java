package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface MaterializedWidgetStateHandler {

	String REFRESH = "refresh";
	String VIEW_NAME = "viewName";

	Map<String, Object> handleWidgetState(Widget widget, MultiValueMap<String, String> params);
}
