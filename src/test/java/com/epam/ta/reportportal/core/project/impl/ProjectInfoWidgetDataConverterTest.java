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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.IsoFields;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectInfoWidgetDataConverterTest {

	private ProjectInfoWidgetDataConverter converter;

	private String thisWeekFormattedDate;

	private LocalDate today;
	private LocalDate yesterday;

	private String todayString;
	private String yesterdayString;

	@BeforeEach
	void setUp() {
		converter = new ProjectInfoWidgetDataConverter(ImmutableMap.<InfoInterval, ProjectInfoWidgetDataConverter.ProjectInfoGroup>builder()
				.put(InfoInterval.ONE_MONTH, ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_DAY)
				.put(InfoInterval.THREE_MONTHS, ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_WEEK)
				.put(InfoInterval.SIX_MONTHS, ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_WEEK)
				.build());

		thisWeekFormattedDate = LocalDate.now(ZoneOffset.UTC)
				.format(new DateTimeFormatterBuilder().appendValue(IsoFields.WEEK_BASED_YEAR, 4)
						.appendLiteral("-W")
						.appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
						.toFormatter());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		LocalDate now = LocalDate.now(ZoneOffset.UTC);
		today = now.getDayOfWeek().equals(DayOfWeek.MONDAY) ? now.plusDays(2) : now;
		yesterday = today.minusDays(1);

		todayString = today.format(formatter);
		yesterdayString = yesterday.format(formatter);
	}

	@Test
	void getInvestigatedProjectInfo() {
		Map<String, List<ChartObject>> investigatedProjectInfo = converter.getInvestigatedProjectInfo(getTestData(),
				InfoInterval.ONE_MONTH
		);

		assertEquals("42.8", investigatedProjectInfo.get(yesterdayString).get(0).getValues().get("toInvestigate"));
		assertEquals("57.2", investigatedProjectInfo.get(yesterdayString).get(0).getValues().get("investigated"));

		assertEquals("50.0", investigatedProjectInfo.get(todayString).get(0).getValues().get("toInvestigate"));
		assertEquals("50.0", investigatedProjectInfo.get(todayString).get(0).getValues().get("investigated"));
	}

	@Test
	void getInvestigatedProjectInfoWithoutDefectsStatistics() {
		Launch launch = new Launch();
		launch.setName("test_launch");
		launch.setId(1L);
		launch.setNumber(1L);
		launch.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
		launch.setStatistics(Sets.newHashSet(getStatistics(EXECUTIONS_TOTAL, 5), getStatistics(EXECUTIONS_PASSED, 5)));

		Map<String, List<ChartObject>> investigatedProjectInfo = converter.getInvestigatedProjectInfo(Collections.singletonList(launch),
				InfoInterval.THREE_MONTHS
		);

		assertEquals("0", investigatedProjectInfo.get(thisWeekFormattedDate).get(0).getValues().get("toInvestigate"));
		assertEquals("0", investigatedProjectInfo.get(thisWeekFormattedDate).get(0).getValues().get("investigated"));
	}

	@Test
	void getTestCasesStatisticsProjectInfo() {
		Map<String, List<ChartObject>> testCasesStatisticsProjectInfo = converter.getTestCasesStatisticsProjectInfo(getTestData());

		assertEquals("20.0", testCasesStatisticsProjectInfo.get("test_launch").get(0).getValues().get("min"));
		assertEquals("22.0", testCasesStatisticsProjectInfo.get("test_launch").get(0).getValues().get("avg"));
		assertEquals("24.0", testCasesStatisticsProjectInfo.get("test_launch").get(0).getValues().get("max"));
	}

	@Test
	void getLaunchesQuantity() {
		Map<String, List<ChartObject>> launchesQuantity = converter.getLaunchesQuantity(getTestData(), InfoInterval.ONE_MONTH);

		assertEquals("1", launchesQuantity.get(yesterdayString).get(0).getValues().get("count"));
		assertEquals("1", launchesQuantity.get(todayString).get(0).getValues().get("count"));
	}

	@Test
	void getLaunchesQuantityByWeek() {
		Map<String, List<ChartObject>> launchesQuantity = converter.getLaunchesQuantity(getTestData(), InfoInterval.THREE_MONTHS);

		assertEquals("2", launchesQuantity.get(thisWeekFormattedDate).get(0).getValues().get("count"));
	}

	@Test
	void getLaunchesIssues() {
		Map<String, List<ChartObject>> launchesIssues = converter.getLaunchesIssues(getTestData(), InfoInterval.ONE_MONTH);

		assertEquals("3", launchesIssues.get(yesterdayString).get(0).getValues().get("systemIssue"));
		assertEquals("6", launchesIssues.get(yesterdayString).get(0).getValues().get("toInvestigate"));
		assertEquals("2", launchesIssues.get(yesterdayString).get(0).getValues().get("productBug"));
		assertEquals("3", launchesIssues.get(yesterdayString).get(0).getValues().get("automationBug"));

		assertEquals("3", launchesIssues.get(todayString).get(0).getValues().get("systemIssue"));
		assertEquals("8", launchesIssues.get(todayString).get(0).getValues().get("toInvestigate"));
		assertEquals("1", launchesIssues.get(todayString).get(0).getValues().get("productBug"));
		assertEquals("4", launchesIssues.get(todayString).get(0).getValues().get("automationBug"));
	}

	@Test
	void getLaunchesIssuesByWeek() {
		Map<String, List<ChartObject>> launchesIssues = converter.getLaunchesIssues(getTestData(), InfoInterval.THREE_MONTHS);

		assertEquals("6", launchesIssues.get(thisWeekFormattedDate).get(0).getValues().get("systemIssue"));
		assertEquals("14", launchesIssues.get(thisWeekFormattedDate).get(0).getValues().get("toInvestigate"));
		assertEquals("3", launchesIssues.get(thisWeekFormattedDate).get(0).getValues().get("productBug"));
		assertEquals("7", launchesIssues.get(thisWeekFormattedDate).get(0).getValues().get("automationBug"));
	}

	private List<Launch> getTestData() {
		Launch launch1 = new Launch();
		launch1.setName("test_launch");
		launch1.setId(1L);
		launch1.setNumber(1L);
		launch1.setStartTime(LocalDateTime.of(yesterday, LocalTime.now(ZoneOffset.UTC)));
		launch1.setStatistics(Sets.newHashSet(getStatistics(EXECUTIONS_TOTAL, 20),
				getStatistics(EXECUTIONS_PASSED, 5),
				getStatistics(EXECUTIONS_SKIPPED, 1),
				getStatistics(EXECUTIONS_UNTESTED, 2),
				getStatistics(EXECUTIONS_FAILED, 12),
				getStatistics(DEFECTS_AUTOMATION_BUG_TOTAL, 3),
				getStatistics(DEFECTS_PRODUCT_BUG_TOTAL, 2),
				getStatistics(DEFECTS_SYSTEM_ISSUE_TOTAL, 3),
				getStatistics(DEFECTS_TO_INVESTIGATE_TOTAL, 6)
		));
		Launch launch2 = new Launch();
		launch2.setName("test_launch");
		launch2.setId(2L);
		launch2.setNumber(2L);
		launch2.setStartTime(LocalDateTime.of(today, LocalTime.now(ZoneOffset.UTC)));
		launch2.setStatistics(Sets.newHashSet(getStatistics(EXECUTIONS_TOTAL, 24),
				getStatistics(EXECUTIONS_PASSED, 6),
				getStatistics(EXECUTIONS_SKIPPED, 2),
				getStatistics(EXECUTIONS_UNTESTED, 3),
				getStatistics(EXECUTIONS_FAILED, 13),
				getStatistics(DEFECTS_AUTOMATION_BUG_TOTAL, 4),
				getStatistics(DEFECTS_PRODUCT_BUG_TOTAL, 1),
				getStatistics(DEFECTS_SYSTEM_ISSUE_TOTAL, 3),
				getStatistics(DEFECTS_TO_INVESTIGATE_TOTAL, 8)
		));
		return Arrays.asList(launch1, launch2);
	}

	private Statistics getStatistics(String statisticsFieldName, int counter) {
		Statistics statistics = new Statistics();
		statistics.setStatisticsField(new StatisticsField(statisticsFieldName));
		statistics.setCounter(counter);
		return statistics;
	}
}