package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import java.util.Map;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class SearchCollectorFactory {

	private Map<SearchLogsMode, SearchLaunchesCollector> mapping;

	public SearchCollectorFactory(Map<SearchLogsMode, SearchLaunchesCollector> mapping) {
		this.mapping = mapping;
	}

	public SearchLaunchesCollector getCollector(SearchLogsMode mode) {
		return mapping.get(mode);
	}
}
