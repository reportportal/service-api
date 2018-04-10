/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
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
