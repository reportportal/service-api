package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.events.widget.GenerateWidgetViewEvent;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.HealthCheckTableGenerator;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.ViewGenerator;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.REFRESH;
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
	private final Map<WidgetType, ViewGenerator> viewGeneratorMapping = new HashMap<>() {
		{
			put(WidgetType.COMPONENT_HEALTH_CHECK_TABLE, healthCheckTableGenerator);
		}
	};

	private final GenerateWidgetViewEventHandler generateWidgetViewEventHandler;

	{
		healthCheckTableExecutor.setWaitForTasksToCompleteOnShutdown(true);
		healthCheckTableExecutor.setAwaitTerminationSeconds(2);
		generateWidgetViewEventHandler = new GenerateWidgetViewEventHandler(widgetRepository,
				buildFilterStrategyMapping,
				healthCheckTableExecutor,
				viewGeneratorMapping
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

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.put(REFRESH, Collections.singletonList(Boolean.FALSE.toString()));

		GenerateWidgetViewEvent event = new GenerateWidgetViewEvent(1L, params);

		generateWidgetViewEventHandler.onApplicationEvent(event);

		healthCheckTableExecutor.shutdown();

		verify(healthCheckTableGenerator, times(1)).generate(anyBoolean(),
				anyString(),
				any(Widget.class),
				any(Filter.class),
				any(Sort.class),
				any(MultiValueMap.class)
		);

	}

	private Widget getWidget() {

		Widget widget = new Widget();
		widget.setId(1L);
		widget.setWidgetType("componentHealthCheckTable");

		return new WidgetBuilder(widget).addProject(1L).get();
	}

}