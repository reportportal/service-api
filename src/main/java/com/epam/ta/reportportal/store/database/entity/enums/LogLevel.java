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

public enum LogLevel {

	//@formatter:off
	ERROR(LogLevel.ERROR_INT), 
	WARN(LogLevel.WARN_INT), 
	INFO(LogLevel.INFO_INT), 
	DEBUG(LogLevel.DEBUG_INT), 
	TRACE(LogLevel.TRACE_INT),
	FATAL(LogLevel.FATAL_INT),
	UNKNOWN(LogLevel.UNKNOWN_INT);
	//@formatter:on

	public static final int UNKNOWN_INT = 60000;
	public static final int FATAL_INT = 50000;
	public static final int ERROR_INT = 40000;
	public static final int WARN_INT = 30000;
	public static final int INFO_INT = 20000;
	public static final int DEBUG_INT = 10000;
	public static final int TRACE_INT = 5000;

	private int intLevel;

	LogLevel(int intlevel) {
		this.intLevel = intlevel;
	}

	public int toInt() {
		return intLevel;
	}

	/**
	 * Returns <code>true</code> if this Level has a higher or equal Level than the Level passed as
	 * argument, <code>false</code> otherwise.
	 */
	public boolean isGreaterOrEqual(LogLevel r) {
		return intLevel >= r.intLevel;
	}

	/**
	 * Convert the string passed as argument to a Level. If there are no such level throws exception
	 */
	public static LogLevel toLevel(String levelString) {
		return Arrays.stream(LogLevel.values()).filter(level -> level.name().equalsIgnoreCase(levelString)).findAny().orElse(null);
	}

	/**
	 * Convert the string passed as argument to a Level. If level is unknown don't throw exception
	 * and return UNKNOWN
	 */
	public static LogLevel toLevelOrUnknown(String levelString) {
		for (LogLevel level : LogLevel.values()) {
			if (level.name().equalsIgnoreCase(levelString)) {
				return level;
			}
		}
		return UNKNOWN;
	}

	/**
	 * Convert the string passed as argument to a Level
	 */
	public static LogLevel toLevel(int intLevel) {
		switch (intLevel) {
			case UNKNOWN_INT:
				return UNKNOWN;
			case FATAL_INT:
				return FATAL;
			case TRACE_INT:
				return TRACE;
			case DEBUG_INT:
				return DEBUG;
			case INFO_INT:
				return INFO;
			case WARN_INT:
				return WARN;
			case ERROR_INT:
				return ERROR;
			default:
				throw new IllegalArgumentException("Level " + intLevel + " is unknown.");
		}
	}

	@Override
	public String toString() {
		return this.name();
	}

}