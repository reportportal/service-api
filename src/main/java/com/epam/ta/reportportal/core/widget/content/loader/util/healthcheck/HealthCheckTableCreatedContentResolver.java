package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.util.HealthCheckTableGenerator;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.updater.ComponentHealthCheckTableUpdater.STATE;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "healthCheckTableCreatedContentResolver")
public class HealthCheckTableCreatedContentResolver extends AbstractHealthCheckTableContentResolver {

	public static final String CUSTOM_COLUMN = "customColumn";

	private static final String VIEW_PREFIX = "hct";
	private static final String NAME_SEPARATOR = "_";

	protected final TaskExecutor healthCheckTableExecutor;
	protected final HealthCheckTableGenerator healthCheckTableGenerator;
	private final Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;
	private final WidgetRepository widgetRepository;

	public HealthCheckTableCreatedContentResolver(@Qualifier("healthCheckTableExecutor") TaskExecutor healthCheckTableExecutor,
			HealthCheckTableGenerator healthCheckTableGenerator,
			@Qualifier("buildFilterStrategy") Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping,
			WidgetRepository widgetRepository) {
		this.healthCheckTableExecutor = healthCheckTableExecutor;
		this.healthCheckTableGenerator = healthCheckTableGenerator;
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
		this.widgetRepository = widgetRepository;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> getContent(Widget widget, List<String> attributeKeys, List<String> attributeValues) {

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		Map<Filter, Sort> filterSortMapping = buildFilterStrategyMapping.get(widgetType).buildFilter(widget);
		Filter launchesFilter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Sort launchesSort = GROUP_SORTS.apply(filterSortMapping.values());

		widgetRepository.save(new WidgetBuilder(widget).addOption(STATE, WidgetState.RENDERING.getValue()).get());

		generateContent(widget, attributeKeys, launchesFilter, launchesSort);
		return emptyMap();
	}

	protected void generateContent(Widget widget, List<String> attributeKeys, Filter launchesFilter, Sort launchesSort) {
		CompletableFuture.runAsync(() -> healthCheckTableGenerator.generate(false, getInitParams(widget, attributeKeys),
				widget,
				launchesFilter,
				launchesSort
		), healthCheckTableExecutor);
	}

	protected HealthCheckTableInitParams getInitParams(Widget widget, List<String> attributeKeys) {

		String viewName = generateViewName(widget);

		return ofNullable(WidgetOptionUtil.getValueByKey(CUSTOM_COLUMN, widget.getWidgetOptions())).map(custom -> HealthCheckTableInitParams
				.of(viewName, attributeKeys, custom)).orElseGet(() -> HealthCheckTableInitParams.of(viewName, attributeKeys));
	}

	private String generateViewName(Widget widget) {
		return String.join(NAME_SEPARATOR, VIEW_PREFIX, String.valueOf(widget.getProject().getId()), String.valueOf(widget.getId()));
	}
}
