package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.entity.widget.Widget;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class FailedMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

	private final MaterializedWidgetStateHandler refreshWidgetStateHandler;

	@Autowired
	public FailedMaterializedWidgetStateHandler(
			@Qualifier("createdMaterializedWidgetStateHandler") MaterializedWidgetStateHandler refreshWidgetStateHandler) {
		this.refreshWidgetStateHandler = refreshWidgetStateHandler;
	}

	@Override
	public Map<String, Object> handleWidgetState(Widget widget, MultiValueMap<String, String> params) {
		if (BooleanUtils.toBoolean(params.getFirst(REFRESH))) {
			return refreshWidgetStateHandler.handleWidgetState(widget, params);
		}
		return Collections.emptyMap();
	}
}
