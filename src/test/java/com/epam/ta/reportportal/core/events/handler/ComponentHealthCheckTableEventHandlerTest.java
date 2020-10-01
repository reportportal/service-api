package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.events.widget.GenerateComponentHealthCheckTableEvent;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck.util.HealthCheckTableGenerator;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableInitParams;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ComponentHealthCheckTableEventHandlerTest {

	private final WidgetRepository widgetRepository = mock(WidgetRepository.class);
	private final BuildFilterStrategy buildFilterStrategy = mock(BuildFilterStrategy.class);
	private final Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping = ImmutableMap.<WidgetType, BuildFilterStrategy>builder().put(
			WidgetType.COMPONENT_HEALTH_CHECK_TABLE,
			buildFilterStrategy
	).build();
	private ThreadPoolTaskExecutor healthCheckTableExecutor = new ThreadPoolTaskExecutor();

	private final HealthCheckTableGenerator healthCheckTableGenerator = mock(HealthCheckTableGenerator.class);

	private final ComponentHealthCheckTableEventHandler componentHealthCheckTableEventHandler;

	{
		healthCheckTableExecutor.setWaitForTasksToCompleteOnShutdown(true);
		healthCheckTableExecutor.setAwaitTerminationSeconds(2);
		componentHealthCheckTableEventHandler = new ComponentHealthCheckTableEventHandler(widgetRepository,
				buildFilterStrategyMapping,
				healthCheckTableExecutor,
				healthCheckTableGenerator
		);

	}

	@BeforeEach
	public void init() {
		healthCheckTableExecutor.initialize();
	}

	@Test
	void shouldGenerate() {
		Widget widget = getWidget();
		when(widgetRepository.findById(anyLong())).thenReturn(Optional.of(widget));

		Map<Filter, Sort> filterSortMap = new HashMap<>();
		Filter filter = Filter.builder().withTarget(Widget.class).withCondition(FilterCondition.builder().eq("id", "1").build()).build();
		Sort sort = Sort.unsorted();
		filterSortMap.put(filter, sort);

		when(buildFilterStrategy.buildFilter(widget)).thenReturn(filterSortMap);

		GenerateComponentHealthCheckTableEvent event = new GenerateComponentHealthCheckTableEvent(1L, false);

		componentHealthCheckTableEventHandler.onApplicationEvent(event);

		healthCheckTableExecutor.shutdown();

		verify(healthCheckTableGenerator, times(1)).generate(anyBoolean(),
				any(HealthCheckTableInitParams.class),
				any(Widget.class),
				any(Filter.class),
				any(Sort.class)
		);

	}

	private Widget getWidget() {

		Widget widget = new Widget();
		widget.setId(1L);
		widget.setWidgetType("componentHealthCheckTable");

		return new WidgetBuilder(widget).addProject(1L).get();
	}

}