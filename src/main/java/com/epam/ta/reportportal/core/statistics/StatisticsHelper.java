package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Set;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class StatisticsHelper {

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
		return (field.contains("failed") || field.contains("skipped") || field.contains("to_investigate")) && counter > 0;
	};
}
