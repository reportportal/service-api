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
 * Interruption job delay parameters enumerator<br>
 * User for supporting UI types of project parameter
 *
 * @author Andrei_Ramanchuk
 */
public enum InterruptionJobDelay {

	//@formatter:off
	ONE_HOUR("1 hour", 1L),
	THREE_HOURS("3 hours", 3L),
	SIX_HOURS("6 hours", 6L),
	TWELVE_HOURS("12 hours", 12L),
	ONE_DAY("1 day", 24L),
	ONE_WEEK("1 week", 168L);
	//@formatter:on

	private String value;

	private long period;

	public String getValue() {
		return value;
	}

	public long getPeriod() {
		return period;
	}

	InterruptionJobDelay(String delay, long time) {
		this.value = delay;
		this.period = time;
	}

	public static InterruptionJobDelay getByName(String type) {
		return InterruptionJobDelay.valueOf(type);
	}

	public static InterruptionJobDelay findByName(String name) {
		return Arrays.stream(InterruptionJobDelay.values()).filter(delay -> delay.getValue().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}
}