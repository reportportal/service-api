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

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class StatisticsHelper {

	private static final String TOTAL = "statistics$executions$total";
	private static final String PASSED = "statistics$executions$passed";
	private static final String SKIPPED = "statistics$executions$skipped";
	private static final String FAILED = "statistics$executions$failed";
	private static final String UNTESTED = "statistics$executions$untested";

	private StatisticsHelper() {
		//static only
	}

	public static StatusEnum getStatusFromStatistics(Set<Statistics> statistics) {
		return statistics.stream().anyMatch(FAILED_PREDICATE) ? StatusEnum.FAILED : StatusEnum.PASSED;
	}

	private final static Predicate<Statistics> FAILED_PREDICATE = statistics -> {
		StatisticsField statisticsField = ofNullable(statistics.getStatisticsField()).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
				"Statistics should contain a name field."
		));
		String field = statisticsField.getName();
		Integer counter = statistics.getCounter();
		return (field.contains("failed") || field.contains("skipped") || field.contains("untested") || field.contains("to_investigate")) && counter > 0;
	};

	public static Integer extractStatisticsCount(String statisticsField, Set<Statistics> statistics) {
		return statistics.stream()
				.filter(it -> it.getStatisticsField().getName().equalsIgnoreCase(statisticsField))
				.findFirst()
				.orElse(new Statistics())
				.getCounter();
	}

	public static Stream<String> defaultStatisticsFields() {
		return Stream.concat(
				Arrays.stream(TestItemIssueGroup.values())
						.filter(value -> !value.getIssueCounterField().equalsIgnoreCase(NOT_ISSUE_FLAG.getIssueCounterField()))
						.map(value -> "statistics$defects$" + value.getValue().toLowerCase() + "$" + value.getLocator()),
				Stream.of(TOTAL, PASSED, SKIPPED, FAILED, UNTESTED)
		);
	}
}
