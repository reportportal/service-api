package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.util.HealthCheckTableGenerator;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "healthCheckTableRefreshContentResolver")
public class HealthCheckTableRefreshContentResolver extends HealthCheckTableCreatedContentResolver {

	public HealthCheckTableRefreshContentResolver(TaskExecutor healthCheckTableExecutor,
			HealthCheckTableGenerator healthCheckTableGenerator, Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping,
			WidgetRepository widgetRepository) {
		super(healthCheckTableExecutor, healthCheckTableGenerator, buildFilterStrategyMapping, widgetRepository);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void generateContent(Widget widget, List<String> attributeKeys, Filter launchesFilter, Sort launchesSort) {
		CompletableFuture.runAsync(() -> healthCheckTableGenerator.generate(true, getInitParams(widget, attributeKeys),
				widget,
				launchesFilter,
				launchesSort
		), healthCheckTableExecutor);
	}
}
