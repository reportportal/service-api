package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
public class SearchCollectorConfig {

	private ApplicationContext applicationContext;

	public SearchCollectorConfig(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Bean("searchModeMapping")
	public Map<SearchLogsMode, SearchLaunchesCollector> getSearchModeMapping() {
		return ImmutableMap.<SearchLogsMode, SearchLaunchesCollector>builder().put(SearchLogsMode.BY_LAUNCH_NAME,
				applicationContext.getBean(LaunchNameCollector.class)
		)
				.put(SearchLogsMode.CURRENT_LAUNCH, applicationContext.getBean(CurrentLaunchCollector.class))
				.put(SearchLogsMode.FILTER, applicationContext.getBean(FilterCollector.class))
				.build();
	}

	@Bean
	public SearchCollectorFactory searchCollectorFactory() {
		return new SearchCollectorFactory(getSearchModeMapping());
	}
}
