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

/**
 * Keep Launches Delay project parameter enumerator<br>
 * Describe possible values for 'Keep launches' parameter on UI project page
 *
 * @author Andrei_Ramanchuk
 */
public enum KeepLogsDelay {

	//@formatter:off
	TWO_WEEKS("2 weeks", 14L),
	ONE_MONTH("1 month", 30L),
	THREE_MONTHS("3 months", 91L),
	SIX_MONTHS("6 months", 183L),
	FOREVER("forever", 0);
	//@formatter:on

	private String value;

	private long days;

	public String getValue() {
		return value;
	}

	public long getDays() {
		return days;
	}

	KeepLogsDelay(String delay, long period) {
		this.value = delay;
		this.days = period;
	}

	public static KeepLogsDelay getByName(String type) {
		return KeepLogsDelay.valueOf(type);
	}

	public static KeepLogsDelay findByName(String name) {
		return Arrays.stream(KeepLogsDelay.values()).filter(delay -> delay.getValue().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}
}