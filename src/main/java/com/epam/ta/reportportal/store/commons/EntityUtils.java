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

package com.epam.ta.reportportal.store.commons;

import com.google.common.base.Preconditions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Some useful utils for working with entities<br>
 * For example: usernames, project names, tags, etc.
 *
 * @author Andrei Varabyeu
 */
public class EntityUtils {

	private static final String OLD_SEPARATOR = ",";
	private static final String NEW_SEPARATOR = "_";

	private EntityUtils() {

	}

	public static final Function<Date, LocalDateTime> TO_LOCAL_DATE_TIME = date -> {
		Preconditions.checkNotNull(date, "Provided value shouldn't be null");
		return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
	};

	public static final Function<LocalDateTime, Date> TO_DATE = localDateTime -> {
		Preconditions.checkNotNull(localDateTime, "Provided value shouldn't be null");
		return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
	};

	/**
	 * Remove leading and trailing spaces from list of string
	 */
	public static final Function<String, String> TRIM_FUNCTION = String::trim;
	public static final Predicate<String> NOT_EMPTY = s -> !isNullOrEmpty(s);

	/**
	 * Convert declined symbols on allowed for WS and UI
	 */
	public static final Function<String, String> REPLACE_SEPARATOR = s -> s.replace(OLD_SEPARATOR, NEW_SEPARATOR);

	/**
	 * Normalize any ID for database ID fields, for example
	 *
	 * @param id ID to normalize
	 * @return String
	 */

	public static String normalizeId(String id) {
		return Preconditions.checkNotNull(id, "Provided value shouldn't be null").toLowerCase();
	}

}
