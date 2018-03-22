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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.base.Preconditions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Some useful utils for working with entities<br>
 * For example: usernames, project names, tags, etc.
 *
 * @author Andrei Varabyeu
 */
public class EntityUtils {

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
	 * Normalize any ID for database ID fields, for example
	 *
	 * @param id ID to normalize
	 * @return String
	 */

	public static String normalizeId(String id) {
		return Preconditions.checkNotNull(id, "Provided value shouldn't be null").toLowerCase();
	}

	public static ReportPortalUser.ProjectDetails takeProjectDetails(ReportPortalUser user, String projectName) {
		return Optional.ofNullable(user.getProjectDetails().get(projectName))
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
	}

	/**
	 * Remove leading and trailing spaces from list of string
	 *
	 * @param strings Strings to trim
	 * @return String
	 */
	public static Iterable<String> trimStrings(Iterable<String> strings) {
		Preconditions.checkNotNull(strings, "List of strings shouldn't be null");
		return stream(strings.spliterator(), false).filter(string -> !isNullOrEmpty(string)).map(String::trim).collect(toList());
	}

	/**
	 * Convert declined symbols on allowed for WS and UI
	 *
	 * @param input Input to be escaped
	 * @return Updated input
	 */
	public static Iterable<String> update(Iterable<String> input) {
		final String oldSeparator = ",";
		final String newSeparator = "_";
		return stream(input.spliterator(), false).map(string -> string.replace(oldSeparator, newSeparator)).collect(toList());
	}
}
