package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.widget.GenerateComponentHealthCheckTableEvent;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.util.HealthCheckTableGenerator;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ComponentHealthCheckTableEventHandler {

	public static final String CUSTOM_COLUMN = "customColumn";

	private static final String VIEW_PREFIX = "hct";
	private static final String NAME_SEPARATOR = "_";

	private final WidgetRepository widgetRepository;
	private final Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;
	private final TaskExecutor healthCheckTableExecutor;
	private final HealthCheckTableGenerator healthCheckTableGenerator;

	@Autowired
	public ComponentHealthCheckTableEventHandler(WidgetRepository widgetRepository,
			@Qualifier("buildFilterStrategy") Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping,
			@Qualifier("healthCheckTableExecutor") TaskExecutor healthCheckTableExecutor,
			HealthCheckTableGenerator healthCheckTableGenerator) {
		this.widgetRepository = widgetRepository;
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
		this.healthCheckTableExecutor = healthCheckTableExecutor;
		this.healthCheckTableGenerator = healthCheckTableGenerator;
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void onApplicationEvent(GenerateComponentHealthCheckTableEvent event) {
		widgetRepository.findById(event.getWidgetId()).ifPresent(widget -> {
			BusinessRule.expect(widget.getWidgetType(), type -> WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType().equals(type))
					.verify(ErrorType.UNABLE_TO_CREATE_WIDGET,
							Suppliers.formattedSupplier("Wrong widget type '{}'. Expected '{}'",
									widget.getWidgetType(),
									WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType()
							).get()
					);

			Map<Filter, Sort> filterSortMapping = buildFilterStrategyMapping.get(WidgetType.COMPONENT_HEALTH_CHECK_TABLE)
					.buildFilter(widget);
			Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
			Sort launchesSort = GROUP_SORTS.apply(filterSortMapping.values());

			healthCheckTableExecutor.execute(() -> healthCheckTableGenerator.generate(event.isRefresh(),
					getInitParams(widget, event.getAttributeKeys()),
					widget,
					launchesFilter,
					launchesSort
			));
		});
	}

	private HealthCheckTableInitParams getInitParams(Widget widget, List<String> attributeKeys) {

		String viewName = generateViewName(widget);

		return ofNullable(WidgetOptionUtil.getValueByKey(CUSTOM_COLUMN, widget.getWidgetOptions())).map(custom -> HealthCheckTableInitParams
				.of(viewName, attributeKeys, custom)).orElseGet(() -> HealthCheckTableInitParams.of(viewName, attributeKeys));
	}

	private String generateViewName(Widget widget) {
		return String.join(NAME_SEPARATOR, VIEW_PREFIX, String.valueOf(widget.getProject().getId()), String.valueOf(widget.getId()));
	}

}
