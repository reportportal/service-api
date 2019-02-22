package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

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
}