/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.strategy;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public enum AnalyzeItemsMode {

	TO_INVESTIGATE("TO_INVESTIGATE"),
	AUTO_ANALYZED("AUTO_ANALYZED"),
	MANUALLY_ANALYZED("MANUALLY_ANALYZED");

	private String value;

	AnalyzeItemsMode(String value) {
		this.value = value;
	}

	public static AnalyzeItemsMode fromString(String mode) {
		return Arrays.stream(AnalyzeItemsMode.values())
				.filter(it -> it.getValue().equalsIgnoreCase(mode))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(
						ErrorType.INCORRECT_REQUEST,
						"Incorrect analyze items mode. Allowed are: " + Arrays.stream(AnalyzeItemsMode.values())
								.map(AnalyzeItemsMode::getValue)
								.collect(Collectors.toList())
				));
	}

	public String getValue() {
		return value;
	}

}
