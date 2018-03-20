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
 * Get project information intervals<br>
 * Available values:
 * <ul>
 * <li>1 week</li>
 * <li>3 months (by default)</li>
 * <li>6 months</li>
 * </ul>
 *
 * @author Andrei_Ramanchuk
 */
public enum InfoInterval {

	//@formatter:off
	ONE_MONTH("1M", 1),
	THREE_MONTHS("3M", 3),
	SIX_MONTHS("6M", 6);
	//@formatter:on

	private String interval;
	private Integer counter;

	public String getInterval() {
		return interval;
	}

	public Integer getCount() {
		return counter;
	}

	InfoInterval(String value, Integer count) {
		this.interval = value;
		this.counter = count;
	}

	public static InfoInterval getByName(String name) {
		return InfoInterval.valueOf(name);
	}

	public static InfoInterval findByName(String name) {
		return Arrays.stream(InfoInterval.values())
				.filter(interval -> interval.getInterval().equalsIgnoreCase(name))
				.findAny()
				.orElse(null);
	}
}