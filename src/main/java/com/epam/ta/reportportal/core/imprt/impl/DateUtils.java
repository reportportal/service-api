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
package com.epam.ta.reportportal.core.imprt.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Pavel Bortnik
 */
public final class DateUtils {

	private DateUtils() {
		//static only
	}

	public static Date toDate(LocalDateTime startTime) {
		if (null != startTime) {
			return Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}

	/**
	 * Converts string representation of seconds to millis
	 *
	 * @param duration String seconds
	 * @return long millis
	 */
	public static long toMillis(String duration) {
		if (null != duration) {
			Double value = Double.valueOf(duration) * 1000;
			return value.longValue();
		}
		return 0;
	}

}
