/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.project.impl.ProjectInfoWidgetDataConverter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.*;
import com.epam.ta.reportportal.core.widget.content.loader.*;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.CumulativeTrendChartContentLoaderImpl;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.HealthCheckTableReadyContentLoader;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.MaterializedWidgetContentLoader;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.CumulativeTrendChartViewGenerator;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.HealthCheckTableGenerator;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.generator.ViewGenerator;
import com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.*;
import com.epam.ta.reportportal.core.widget.content.loader.util.ProductStatusContentLoaderManager;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * Configuration related to widgets.
 *
 * @author Pavel_Bortnik
 */

@Configuration
public class WidgetConfig implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean("contentLoader")
	public Map<WidgetType, LoadContentStrategy> contentLoader() {
		return ImmutableMap.<WidgetType, LoadContentStrategy>builder().put(WidgetType.FLAKY_TEST_CASES,
				applicationContext.getBean(FlakyCasesTableContentLoader.class)
		)
				.put(WidgetType.OVERALL_STATISTICS, applicationContext.getBean(OverallStatisticsContentLoader.class))
				.put(WidgetType.PASSING_RATE_SUMMARY, applicationContext.getBean(PassingRateSummaryContentLoader.class))
				.put(WidgetType.OLD_LINE_CHART, applicationContext.getBean(LineChartContentLoader.class))
				.put(WidgetType.INVESTIGATED_TREND, applicationContext.getBean(ChartInvestigatedContentLoader.class))
				.put(WidgetType.STATISTIC_TREND, applicationContext.getBean(LineChartContentLoader.class))
				.put(WidgetType.LAUNCH_STATISTICS, applicationContext.getBean(LaunchExecutionAndIssueStatisticsContentLoader.class))
				.put(WidgetType.CASES_TREND, applicationContext.getBean(CasesTrendContentLoader.class))
				.put(WidgetType.NOT_PASSED, applicationContext.getBean(NotPassedTestsContentLoader.class))
				.put(WidgetType.UNIQUE_BUG_TABLE, applicationContext.getBean(UniqueBugContentLoader.class))
				.put(WidgetType.BUG_TREND, applicationContext.getBean(BugTrendChartContentLoader.class))
				.put(WidgetType.ACTIVITY, applicationContext.getBean(ActivityContentLoader.class))
				.put(WidgetType.LAUNCHES_COMPARISON_CHART, applicationContext.getBean(LaunchesComparisonContentLoader.class))
				.put(WidgetType.LAUNCHES_DURATION_CHART, applicationContext.getBean(LaunchesDurationContentLoader.class))
				.put(WidgetType.LAUNCHES_TABLE, applicationContext.getBean(LaunchesTableContentLoader.class))
				.put(WidgetType.TOP_TEST_CASES, applicationContext.getBean(TopTestCasesContentLoader.class))
				.put(WidgetType.PASSING_RATE_PER_LAUNCH, applicationContext.getBean(PassingRatePerLaunchContentLoader.class))
				.put(WidgetType.PRODUCT_STATUS, applicationContext.getBean(ProductStatusContentLoaderManager.class))
				.put(WidgetType.MOST_TIME_CONSUMING, applicationContext.getBean(MostTimeConsumingContentLoader.class))
				.build();
	}

	@Bean("multilevelContentLoader")
	public Map<WidgetType, MultilevelLoadContentStrategy> multilevelContentLoader() {
		return ImmutableMap.<WidgetType, MultilevelLoadContentStrategy>builder().put(WidgetType.TOP_PATTERN_TEMPLATES,
				applicationContext.getBean(TopPatternContentLoader.class)
		)
				.put(WidgetType.COMPONENT_HEALTH_CHECK, applicationContext.getBean(ComponentHealthCheckContentLoader.class))
				.build();
	}

	@Bean("buildFilterStrategy")
	public Map<WidgetType, BuildFilterStrategy> buildFilterStrategy() {
		return ImmutableMap.<WidgetType, BuildFilterStrategy>builder().put(WidgetType.OLD_LINE_CHART,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		)
				.put(WidgetType.INVESTIGATED_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.STATISTIC_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.LAUNCH_STATISTICS, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.OVERALL_STATISTICS, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.CASES_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.NOT_PASSED, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.BUG_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.LAUNCHES_TABLE, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.PASSING_RATE_SUMMARY,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.CUMULATIVE, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.PRODUCT_STATUS, (ProductStatusFilterStrategy) applicationContext.getBean("productStatusFilterStrategy"))
				.put(WidgetType.UNIQUE_BUG_TABLE, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.ACTIVITY, (ActivityFilterStrategy) applicationContext.getBean("activityFilterStrategy"))
				.put(WidgetType.LAUNCHES_COMPARISON_CHART,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.LAUNCHES_DURATION_CHART,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.TOP_TEST_CASES, (LaunchHistoryFilterStrategy) applicationContext.getBean("launchHistoryFilterStrategy"))
				.put(WidgetType.PASSING_RATE_PER_LAUNCH,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.FLAKY_TEST_CASES, (GeneralLaunchFilterStrategy) applicationContext.getBean("launchHistoryFilterStrategy"))
				.put(WidgetType.MOST_TIME_CONSUMING, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"))
				.put(WidgetType.TOP_PATTERN_TEMPLATES,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.COMPONENT_HEALTH_CHECK,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.put(WidgetType.COMPONENT_HEALTH_CHECK_TABLE,
						(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
				)
				.build();
	}

	@Bean("productStatusContentLoader")
	public Map<String, ProductStatusContentLoader> productStatusContentLoader() {
		return ImmutableMap.<String, ProductStatusContentLoader>builder().put("launch",
				applicationContext.getBean(ProductStatusLaunchGroupedContentLoader.class)
		)
				.put("filter", applicationContext.getBean(ProductStatusFilterGroupedContentLoader.class))
				.build();
	}

	@Bean("groupingStrategy")
	public Map<InfoInterval, ProjectInfoWidgetDataConverter.ProjectInfoGroup> group() {
		return ImmutableMap.<InfoInterval, ProjectInfoWidgetDataConverter.ProjectInfoGroup>builder().put(InfoInterval.ONE_MONTH,
				ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_DAY
		)
				.put(InfoInterval.THREE_MONTHS, ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_WEEK)
				.put(InfoInterval.SIX_MONTHS, ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_WEEK)
				.build();
	}

	@Bean("unfilteredWidgetTypes")
	public Set<WidgetType> unfilteredWidgetTypes() {
		return ImmutableSet.<WidgetType>builder().add(WidgetType.ACTIVITY)
				.add(WidgetType.TOP_TEST_CASES)
				.add(WidgetType.PASSING_RATE_PER_LAUNCH)
				.add(WidgetType.MOST_TIME_CONSUMING)
				.add(WidgetType.FLAKY_TEST_CASES)
				.build();
	}

	@Bean("widgetStateHandlerMapping")
	public Map<WidgetState, MaterializedWidgetStateHandler> widgetStateHandlerMapping() {
		return ImmutableMap.<WidgetState, MaterializedWidgetStateHandler>builder().put(WidgetState.CREATED,
				applicationContext.getBean(CreatedMaterializedWidgetStateHandler.class)
		)
				.put(WidgetState.READY, applicationContext.getBean(ReadyMaterializedWidgetStateHandler.class))
				.put(WidgetState.RENDERING, applicationContext.getBean(EmptyMaterializedWidgetStateHandler.class))
				.put(WidgetState.FAILED, applicationContext.getBean(FailedMaterializedWidgetStateHandler.class))
				.build();
	}

	@Bean("materializedWidgetContentLoaderMapping")
	public Map<WidgetType, MaterializedWidgetContentLoader> materializedWidgetContentLoaderMapping() {
		return ImmutableMap.<WidgetType, MaterializedWidgetContentLoader>builder().put(WidgetType.COMPONENT_HEALTH_CHECK_TABLE,
				applicationContext.getBean(HealthCheckTableReadyContentLoader.class)
		)
				.put(WidgetType.CUMULATIVE, applicationContext.getBean(CumulativeTrendChartContentLoaderImpl.class))
				.build();
	}

	@Bean("viewGeneratorMapping")
	public Map<WidgetType, ViewGenerator> viewGeneratorMapping() {
		return ImmutableMap.<WidgetType, ViewGenerator>builder().put(WidgetType.COMPONENT_HEALTH_CHECK_TABLE,
				applicationContext.getBean(HealthCheckTableGenerator.class)
		)
				.put(WidgetType.CUMULATIVE, applicationContext.getBean(CumulativeTrendChartViewGenerator.class))
				.build();
	}

}
