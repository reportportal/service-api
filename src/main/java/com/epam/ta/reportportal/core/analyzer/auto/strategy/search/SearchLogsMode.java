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
