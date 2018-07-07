/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.filter.*;
import com.epam.ta.reportportal.core.widget.content.loader.*;
import com.epam.ta.reportportal.store.database.entity.widget.WidgetType;
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
		mapping.put(WidgetType.OVERALL_STATISTICS, applicationContext.getBean(OverallStatisticsContentLoader.class));
		mapping.put(WidgetType.PASSING_RATE_SUMMARY, applicationContext.getBean(OverallStatisticsContentLoader.class));
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
		mapping.put(WidgetType.CUMULATIVE, applicationContext.getBean(CumulativeChartContentLoader.class));
		return mapping;
	}

	@Bean("buildFilterStrategy")
	public Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping() {
		Map<WidgetType, BuildFilterStrategy> mapping = new HashMap<>();
		mapping.put(WidgetType.OLD_LINE_CHART, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.INVESTIGATED_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.STATISTIC_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.LAUNCH_STATISTICS, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.OVERALL_STATISTICS, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.CASES_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.NOT_PASSED, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.BUG_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.LAUNCHES_TABLE, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.PASSING_RATE_SUMMARY, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(WidgetType.CUMULATIVE, applicationContext.getBean(GeneralFilterStrategy.class));

		mapping.put(WidgetType.UNIQUE_BUG_TABLE, applicationContext.getBean(UniqueBugFilterStrategy.class));
		mapping.put(WidgetType.ACTIVITY, applicationContext.getBean(ActivityFilterStrategy.class));
		mapping.put(WidgetType.LAUNCHES_COMPARISON_CHART, applicationContext.getBean(CompareLaunchesFilterStrategy.class));
		mapping.put(WidgetType.LAUNCHES_DURATION_CHART, applicationContext.getBean(CompareLaunchesFilterStrategy.class));
		//mapping.put(WidgetType.MOST_FAILED_TEST_CASES, applicationContext.getBean(MostFailedTestCasesFilterStrategy.class));
		mapping.put(WidgetType.PASSING_RATE_PER_LAUNCH, applicationContext.getBean(PassingRateFilterStrategy.class));
		//mapping.put(WidgetType.FLAKY_TEST_CASES, applicationContext.getBean(FlakyTestCasesStrategy.class));
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
