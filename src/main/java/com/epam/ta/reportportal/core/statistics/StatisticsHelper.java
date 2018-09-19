package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.statistics.Statistics;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Ivan Budaev
 */
public final class StatisticsHelper {

	private StatisticsHelper() {
		//static only
	}

	public static StatusEnum getStatusFromStatistics(Set<Statistics> statistics) {
		return statistics.stream().anyMatch(FAILED_PREDICATE) ? StatusEnum.FAILED : StatusEnum.PASSED;
	}

	private final static Predicate<Statistics> FAILED_PREDICATE = statistics -> {
		String field = statistics.getField();
		Integer counter = statistics.getCounter();
		return (field.contains("failed") || field.contains("skipped") || field.contains("to_investigate")) && counter > 0;
	};
}
