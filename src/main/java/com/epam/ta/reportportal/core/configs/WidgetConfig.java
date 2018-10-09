/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.GeneralLaunchFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.LaunchHistoryFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.ProductStatusFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.ProjectFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.*;
import com.epam.ta.reportportal.core.widget.content.loader.util.ProductStatusContentLoaderManager;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
	public Map<WidgetType, LoadContentStrategy> contentLoadingMapping() {
		Map<WidgetType, LoadContentStrategy> mapping = new HashMap<>();
		mapping.put(WidgetType.FLAKY_TEST_CASES, applicationContext.getBean(FlakyCasesTableContentLoader.class));
		mapping.put(WidgetType.OVERALL_STATISTICS, applicationContext.getBean(OverallStatisticsContentLoader.class));
		mapping.put(WidgetType.PASSING_RATE_SUMMARY, applicationContext.getBean(PassingRateSummaryContentLoader.class));
		mapping.put(WidgetType.OLD_LINE_CHART, applicationContext.getBean(LineChartContentLoader.class));
		mapping.put(WidgetType.INVESTIGATED_TREND, applicationContext.getBean(ChartInvestigatedContentLoader.class));
		mapping.put(WidgetType.STATISTIC_TREND, applicationContext.getBean(LineChartContentLoader.class));
		mapping.put(WidgetType.LAUNCH_STATISTICS, applicationContext.getBean(LaunchStatisticsChartContentLoader.class));
		mapping.put(WidgetType.CASES_TREND, applicationContext.getBean(CasesTrendContentLoader.class));
		mapping.put(WidgetType.NOT_PASSED, applicationContext.getBean(NotPassedTestsContentLoader.class));
		mapping.put(WidgetType.UNIQUE_BUG_TABLE, applicationContext.getBean(UniqueBugContentLoader.class));
		mapping.put(WidgetType.BUG_TREND, applicationContext.getBean(BugTrendChartContentLoader.class));
		mapping.put(WidgetType.ACTIVITY, applicationContext.getBean(ActivityContentLoader.class));
		mapping.put(WidgetType.LAUNCHES_COMPARISON_CHART, applicationContext.getBean(LaunchesComparisonContentLoader.class));
		mapping.put(WidgetType.LAUNCHES_DURATION_CHART, applicationContext.getBean(LaunchesDurationContentLoader.class));
		mapping.put(WidgetType.LAUNCHES_TABLE, applicationContext.getBean(LaunchesTableContentLoader.class));
		mapping.put(WidgetType.TOP_TEST_CASES, applicationContext.getBean(TopTestCasesContentLoader.class));
		mapping.put(WidgetType.PASSING_RATE_PER_LAUNCH, applicationContext.getBean(PassingRatePerLaunchContentLoader.class));
		mapping.put(WidgetType.PRODUCT_STATUS, applicationContext.getBean(ProductStatusContentLoaderManager.class));
		//		mapping.put(WidgetType.CUMULATIVE, applicationContext.getBean(CumulativeTrendChartLoader.class));
		mapping.put(WidgetType.MOST_TIME_CONSUMING, applicationContext.getBean(MostTimeConsumingContentLoader.class));
		return mapping;
	}

	@Bean("buildFilterStrategyMapping")
	public Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping() {
		Map<WidgetType, BuildFilterStrategy> mapping = new HashMap<>();
		mapping.put(WidgetType.OLD_LINE_CHART, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.INVESTIGATED_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.STATISTIC_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.LAUNCH_STATISTICS, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.OVERALL_STATISTICS, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.CASES_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.NOT_PASSED, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.BUG_TREND, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.LAUNCHES_TABLE, (GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy"));
		mapping.put(WidgetType.PASSING_RATE_SUMMARY,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		);
		//		mapping.put(WidgetType.CUMULATIVE, applicationContext.getBean(GeneralStatisticsFilterStrategy.class));
		mapping.put(WidgetType.PRODUCT_STATUS, (ProductStatusFilterStrategy) applicationContext.getBean("productStatusFilterStrategy"));
		mapping.put(WidgetType.UNIQUE_BUG_TABLE, (ProjectFilterStrategy) applicationContext.getBean("projectFilterStrategy"));
		mapping.put(WidgetType.ACTIVITY, (ProjectFilterStrategy) applicationContext.getBean("projectFilterStrategy"));
		mapping.put(WidgetType.LAUNCHES_COMPARISON_CHART,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		);
		mapping.put(WidgetType.LAUNCHES_DURATION_CHART,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		);
		mapping.put(WidgetType.TOP_TEST_CASES, (LaunchHistoryFilterStrategy) applicationContext.getBean("launchHistoryFilterStrategy"));
		mapping.put(WidgetType.PASSING_RATE_PER_LAUNCH,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		);
		mapping.put(WidgetType.FLAKY_TEST_CASES, (GeneralLaunchFilterStrategy) applicationContext.getBean("launchHistoryFilterStrategy"));
		mapping.put(WidgetType.MOST_TIME_CONSUMING,
				(GeneralLaunchFilterStrategy) applicationContext.getBean("generalLaunchFilterStrategy")
		);
		return mapping;
	}

	@Bean("productStatusContentLoader")
	public Map<String, ProductStatusContentLoader> productStatusContentLoaderMapping() {
		Map<String, ProductStatusContentLoader> mapping = new HashMap<>();
		mapping.put("launch", applicationContext.getBean(ProductStatusLaunchGroupedContentLoader.class));
		mapping.put("filter", applicationContext.getBean(ProductStatusFilterGroupedContentLoader.class));
		return mapping;
	}

	//		@Bean("groupingStrategy")
	//		public Map<InfoInterval, ProjectInfoGroup> groupMapping() {
	//			Map<InfoInterval, ProjectInfoGroup> mapping = new HashMap<>();
	//			mapping.put(InfoInterval.ONE_MONTH, ProjectInfoGroup.BY_DAY);
	//			mapping.put(InfoInterval.THREE_MONTHS, ProjectInfoGroup.BY_WEEK);
	//			mapping.put(InfoInterval.SIX_MONTHS, ProjectInfoGroup.BY_WEEK);
	//			return mapping;
	//		}

}
