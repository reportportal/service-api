package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class EmptyMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

	@Override
	public Map<String, Object> handleWidgetState(Widget widget, MultiValueMap<String, String> params) {
		return Collections.emptyMap();
	}
}
