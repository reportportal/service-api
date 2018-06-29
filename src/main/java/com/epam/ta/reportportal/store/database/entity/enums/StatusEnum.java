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

package com.epam.ta.reportportal.store.database.entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StatusEnum {

	//@formatter:off
	IN_PROGRESS("", false),
	PASSED("passed", true),
	FAILED("failed", false),
	STOPPED("stopped", false), //status for manually stopped launches
	SKIPPED("skipped", false),
	INTERRUPTED("failed", false),
	//RESETED("reseted"), //status for items with deleted descendants
	CANCELLED("cancelled", false); //soupUI specific status
	//@formatter:on

	private final String executionCounterField;

	private final boolean positive;

	StatusEnum(String executionCounterField, boolean isPositive) {
		this.executionCounterField = executionCounterField;
		this.positive = isPositive;
	}

	public static Optional<StatusEnum> fromValue(String value) {
		return Arrays.stream(StatusEnum.values()).filter(status -> status.name().equalsIgnoreCase(value)).findAny();
	}

	public String getExecutionCounterField() {
		return executionCounterField;
	}

	public boolean isPositive() {
		return positive;
	}
}