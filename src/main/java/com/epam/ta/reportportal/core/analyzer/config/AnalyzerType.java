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

package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum AnalyzerType {

	AUTO_ANALYZER("autoAnalyzer", "auto"),
	PATTERN_ANALYZER("patternAnalyzer", "pattern");

	private final String name;
	private final String shortName;

	AnalyzerType(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	public static AnalyzerType fromString(String type) {
		return Arrays.stream(AnalyzerType.values())
				.filter(it -> it.getName().equalsIgnoreCase(type) || it.getShortName().equalsIgnoreCase(type))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						"Incorrect analyzer type. Allowed are: " + Arrays.stream(AnalyzerType.values())
								.map(analyzerType -> analyzerType.getName() + "\\" + analyzerType.getShortName())
								.collect(Collectors.toList())
				));
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}
}
