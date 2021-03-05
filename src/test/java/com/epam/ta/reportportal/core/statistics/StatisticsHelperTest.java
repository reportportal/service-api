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
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class StatisticsHelperTest {

	@Test
	void emptyStatisticsTest() {
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> StatisticsHelper.getStatusFromStatistics(Sets.newHashSet(new Statistics()))
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Statistics should contain a name field.'",
				exception.getMessage()
		);
	}

	@Test
	void passedTest() {
		StatusEnum statusFromStatistics = StatisticsHelper.getStatusFromStatistics(Sets.newHashSet(getStatistics("statistics$executions$passed",
				4
				),
				getStatistics("statistics$executions$total", 4),
				getStatistics("statistics$executions$failed", 0),
				getStatistics("statistics$executions$skipped", 0)
		));
		assertEquals(StatusEnum.PASSED, statusFromStatistics);
	}

	@Test
	void failedTest() {
		StatusEnum statusFromStatistics = StatisticsHelper.getStatusFromStatistics(Sets.newHashSet(getStatistics("statistics$executions$passed",
				4
				),
				getStatistics("statistics$executions$failed", 2),
				getStatistics("statistics$executions$total", 6)
		));
		assertEquals(StatusEnum.FAILED, statusFromStatistics);
	}

	@Test
	void skippedTest() {
		StatusEnum statusFromStatistics = StatisticsHelper.getStatusFromStatistics(Sets.newHashSet(getStatistics("statistics$executions$passed",
				4
				),
				getStatistics("statistics$executions$skipped", 1),
				getStatistics("statistics$executions$total", 5)
		));
		assertEquals(StatusEnum.FAILED, statusFromStatistics);
	}

	@Test
	void toInvestigateTest() {
		StatusEnum statusFromStatistics = StatisticsHelper.getStatusFromStatistics(Sets.newHashSet(getStatistics("statistics$executions$passed",
				4
				),
				getStatistics("statistics$defects$to_investigate$total", 1),
				getStatistics("statistics$executions$total", 5)
		));
		assertEquals(StatusEnum.FAILED, statusFromStatistics);
	}

	private Statistics getStatistics(String statisticsFieldName, int counter) {
		Statistics statistics = new Statistics();
		statistics.setStatisticsField(new StatisticsField(statisticsFieldName));
		statistics.setCounter(counter);
		return statistics;
	}

	@Test
	void extractStatisticsCount() {
		assertEquals(5, (int) StatisticsHelper.extractStatisticsCount(
				"statistics$executions$passed",
				Sets.newHashSet(getStatistics("statistics$executions$passed", 5), getStatistics("statistics$executions$total", 5))
		));
	}

	@Test
	void defaultStatisticsFields() {
		List<String> fields = StatisticsHelper.defaultStatisticsFields().collect(Collectors.toList());
		assertEquals(10, fields.size());
	}
}