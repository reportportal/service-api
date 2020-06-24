package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.updater.ComponentHealthCheckTableUpdater.STATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class HealthCheckTableResolverStrategy {

	private final Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableContentResolverMapping;

	public HealthCheckTableResolverStrategy(Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableContentResolverMapping) {
		this.healthCheckTableContentResolverMapping = healthCheckTableContentResolverMapping;
	}

	public Map<String, Object> resolveContent(Widget widget, String[] attributes, Map<String, String> params) {

		WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE,
				widget.getWidgetOptions()
		)).flatMap(WidgetState::findByName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Widget state not provided"));

		return healthCheckTableContentResolverMapping.get(widgetState).loadContent(widget, attributes, params);

	}

}
