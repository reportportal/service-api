package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.widget.content.updater.ComponentHealthCheckTableUpdater.STATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class HealthCheckTableResolverStrategy {

	private static final String REFRESH = "refresh";

	private final Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableContentResolverMapping;
	private final Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableRefreshResolverMapping;

	@Autowired
	public HealthCheckTableResolverStrategy(@Qualifier("healthCheckTableContentResolverMapping")
			Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableContentResolverMapping,
			@Qualifier("healthCheckTableContentRefreshMapping")
					Map<WidgetState, HealthCheckTableContentResolver> healthCheckTableRefreshResolverMapping) {
		this.healthCheckTableContentResolverMapping = healthCheckTableContentResolverMapping;
		this.healthCheckTableRefreshResolverMapping = healthCheckTableRefreshResolverMapping;
	}

	public Map<String, Object> resolveContent(Widget widget, String[] attributes, Map<String, String> params) {

		WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE,
				widget.getWidgetOptions()
		)).flatMap(WidgetState::findByName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Widget state not provided"));

		if (BooleanUtils.toBoolean(params.get(REFRESH))) {
			HealthCheckTableContentResolver resolver = ofNullable(healthCheckTableRefreshResolverMapping.get(widgetState)).orElseThrow(() -> new ReportPortalException(
					ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
					Suppliers.formattedSupplier("Widget should be in [{}] states to be refreshed",
							healthCheckTableRefreshResolverMapping.keySet()
									.stream()
									.map(WidgetState::getValue)
									.collect(Collectors.joining(","))
					).get()
			));
			return resolver.loadContent(widget, attributes, params);
		} else {
			return healthCheckTableContentResolverMapping.get(widgetState).loadContent(widget, attributes, params);
		}

	}

}
