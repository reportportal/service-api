package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.events.widget.GenerateWidgetViewEvent;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.HealthCheckTableGenerator;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.loader.materialized.MaterializedContentLoader.REFRESH;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GenerateWidgetViewEventHandler {

	private static final String VIEW_PREFIX = "hct";
	private static final String NAME_SEPARATOR = "_";

	private final WidgetRepository widgetRepository;
	private final Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;
	private final TaskExecutor widgetViewExecutor;
	private final HealthCheckTableGenerator healthCheckTableGenerator;

	@Autowired
	public GenerateWidgetViewEventHandler(WidgetRepository widgetRepository,
			@Qualifier("buildFilterStrategy") Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping,
			@Qualifier("widgetViewExecutor") TaskExecutor widgetViewExecutor, HealthCheckTableGenerator healthCheckTableGenerator) {
		this.widgetRepository = widgetRepository;
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
		this.widgetViewExecutor = widgetViewExecutor;
		this.healthCheckTableGenerator = healthCheckTableGenerator;
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void onApplicationEvent(GenerateWidgetViewEvent event) {
		widgetRepository.findById(event.getWidgetId()).ifPresent(widget -> {
			WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
					.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
							formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
					));

			Map<Filter, Sort> filterSortMapping = buildFilterStrategyMapping.get(widgetType).buildFilter(widget);
			Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
			Sort launchesSort = GROUP_SORTS.apply(filterSortMapping.values());

			widgetViewExecutor.execute(() -> healthCheckTableGenerator.generate(BooleanUtils.toBoolean(event.getParams().getFirst(REFRESH)),
					generateViewName(widget),
					widget,
					launchesFilter,
					launchesSort,
					event.getParams()
			));
		});
	}

	private String generateViewName(Widget widget) {
		return String.join(NAME_SEPARATOR, VIEW_PREFIX, String.valueOf(widget.getProject().getId()), String.valueOf(widget.getId()));
	}

}
