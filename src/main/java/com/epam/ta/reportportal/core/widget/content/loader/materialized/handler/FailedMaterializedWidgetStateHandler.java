package com.epam.ta.reportportal.core.widget.content.loader.materialized.handler;

import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "failedMaterializedContentLoader")
public class FailedMaterializedWidgetStateHandler extends CreatedMaterializedWidgetStateHandler {

	@Autowired
	public FailedMaterializedWidgetStateHandler(WidgetRepository widgetRepository, ApplicationEventPublisher eventPublisher) {
		super(widgetRepository, eventPublisher);
	}

	@Override
	public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {
		params.put(REFRESH, Collections.singletonList(Boolean.TRUE.toString()));
		return super.loadContent(widget, params);
	}
}
