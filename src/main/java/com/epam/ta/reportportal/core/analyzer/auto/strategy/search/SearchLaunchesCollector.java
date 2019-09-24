package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import com.epam.ta.reportportal.entity.launch.Launch;

import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface SearchLaunchesCollector {

	int LAUNCHES_FILTER_LIMIT = 10;

	List<Long> collect(Long filerId, Launch launch);
}
