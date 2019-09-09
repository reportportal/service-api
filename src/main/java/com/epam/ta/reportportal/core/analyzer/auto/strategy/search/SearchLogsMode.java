package com.epam.ta.reportportal.core.analyzer.auto.strategy.search;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public enum SearchLogsMode {

	BY_LAUNCH_NAME("launchName"),
	CURRENT_LAUNCH("currentLaunch"),
	FILTER("filter");

	private String value;

	SearchLogsMode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Optional<SearchLogsMode> fromString(String mode) {
		return Arrays.stream(values()).filter(it -> it.getValue().equalsIgnoreCase(mode)).findFirst();
	}
}
