/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.statistics.Statistics;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class StatisticsUtils {

	private StatisticsUtils() {
		//static only
	}

	public static Integer extractStatisticsCount(String statisticsField, Set<Statistics> statistics) {
		return statistics.stream()
				.filter(it -> it.getStatisticsField().getName().equalsIgnoreCase(statisticsField))
				.findFirst()
				.orElse(new Statistics())
				.getCounter();
	}

	public static Stream<String> defaultStatisticsFileds() {
		return Stream.concat(Arrays.stream(TestItemIssueGroup.values())
				.filter(value -> !value.getIssueCounterField().equalsIgnoreCase(NOT_ISSUE_FLAG.getIssueCounterField()))
				.map(value -> "statistics$defects$" + value.getIssueCounterField() + "$" + value.getLocator()), Stream.of(
				"statistics$executions$total",
				"statistics$executions$passed",
				"statistics$executions$skipped",
				"statistics$executions$failed"
		));
	}

}
