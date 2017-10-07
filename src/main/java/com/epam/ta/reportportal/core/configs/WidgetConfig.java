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

import com.epam.ta.reportportal.core.widget.content.*;
import com.epam.ta.reportportal.core.widget.content.history.FlakyTestCasesStrategy;
import com.epam.ta.reportportal.core.widget.content.history.MostFailedTestCasesFilterStrategy;
import com.epam.ta.reportportal.database.entity.project.info.InfoInterval;
import com.epam.ta.reportportal.database.entity.project.info.ProjectInfoGroup;
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
	public Map<GadgetTypes, IContentLoadingStrategy> contentLoadingMapping() {
		Map<GadgetTypes, IContentLoadingStrategy> mapping = new HashMap<>();
		mapping.put(GadgetTypes.OLD_LINE_CHART, applicationContext.getBean(LineChartContentLoader.class));
		mapping.put(GadgetTypes.INVESTIGATED_TREND, applicationContext.getBean(ChartInvestigatedContentLoader.class));
		mapping.put(GadgetTypes.STATISTIC_TREND, applicationContext.getBean(LineChartContentLoader.class));
		mapping.put(GadgetTypes.LAUNCH_STATISTICS, applicationContext.getBean(LaunchStatisticsChartContentLoader.class));
		mapping.put(GadgetTypes.OVERALL_STATISTICS, applicationContext.getBean(OverallStatisticsContentLoader.class));
		mapping.put(GadgetTypes.CASES_TREND, applicationContext.getBean(CasesTrendContentLoader.class));
		mapping.put(GadgetTypes.NOT_PASSED, applicationContext.getBean(NotPassedTestsContentLoader.class));
		mapping.put(GadgetTypes.UNIQUE_BUG_TABLE, applicationContext.getBean(UniqueBugContentLoader.class));
		mapping.put(GadgetTypes.BUG_TREND, applicationContext.getBean(BugTrendChartContentLoader.class));
		mapping.put(GadgetTypes.ACTIVITY, applicationContext.getBean(ActivityContentLoader.class));
		mapping.put(GadgetTypes.LAUNCHES_COMPARISON_CHART, applicationContext.getBean(LaunchesComparisonContentLoader.class));
		mapping.put(GadgetTypes.LAUNCHES_DURATION_CHART, applicationContext.getBean(LaunchesDurationContentLoader.class));
		mapping.put(GadgetTypes.LAUNCHES_TABLE, applicationContext.getBean(LaunchesTableContentLoader.class));
		mapping.put(GadgetTypes.PASSING_RATE_SUMMARY, applicationContext.getBean(OverallStatisticsContentLoader.class));
		mapping.put(GadgetTypes.CUMULATIVE, applicationContext.getBean(CumulativeContentLoader.class));
		return mapping;
	}

	@Bean("buildFilterStrategy")
	public Map<GadgetTypes, BuildFilterStrategy> buildFilterStrategyMapping() {
		Map<GadgetTypes, BuildFilterStrategy> mapping = new HashMap<>();
		mapping.put(GadgetTypes.OLD_LINE_CHART, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.INVESTIGATED_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.STATISTIC_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.LAUNCH_STATISTICS, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.OVERALL_STATISTICS, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.CASES_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.NOT_PASSED, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.UNIQUE_BUG_TABLE, applicationContext.getBean(UniqueBugFilterStrategy.class));
		mapping.put(GadgetTypes.BUG_TREND, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.ACTIVITY, applicationContext.getBean(ActivityFilterStrategy.class));
		mapping.put(GadgetTypes.LAUNCHES_COMPARISON_CHART, applicationContext.getBean(CompareLaunchesFilterStrategy.class));
		mapping.put(GadgetTypes.LAUNCHES_DURATION_CHART, applicationContext.getBean(CompareLaunchesFilterStrategy.class));
		mapping.put(GadgetTypes.LAUNCHES_TABLE, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.PASSING_RATE_SUMMARY, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.MOST_FAILED_TEST_CASES, applicationContext.getBean(MostFailedTestCasesFilterStrategy.class));
		mapping.put(GadgetTypes.PASSING_RATE_PER_LAUNCH, applicationContext.getBean(PassingRateFilterStrategy.class));
		mapping.put(GadgetTypes.CUMULATIVE, applicationContext.getBean(GeneralFilterStrategy.class));
		mapping.put(GadgetTypes.FLAKY_TEST_CASES, applicationContext.getBean(FlakyTestCasesStrategy.class));
		return mapping;
	}

	@Bean("groupingStrategy")
	public Map<InfoInterval, ProjectInfoGroup> groupMapping() {
		Map<InfoInterval, ProjectInfoGroup> mapping = new HashMap<>();
		mapping.put(InfoInterval.ONE_MONTH, ProjectInfoGroup.BY_DAY);
		mapping.put(InfoInterval.THREE_MONTHS, ProjectInfoGroup.BY_WEEK);
		mapping.put(InfoInterval.SIX_MONTHS, ProjectInfoGroup.BY_WEEK);
		return mapping;
	}

}
