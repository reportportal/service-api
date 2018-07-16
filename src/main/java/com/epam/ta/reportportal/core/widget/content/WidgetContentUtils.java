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

package com.epam.ta.reportportal.core.widget.content;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * @author Pavel Bortnik
 */
public final class WidgetContentUtils {

	private static final String SPLITTING_REGEX = "\\$";

	private WidgetContentUtils() {
		//static only
	}

	public static final Function<List<String>, Map<String, List<String>>> GROUP_CONTENT_FIELDS = contentFields -> contentFields.stream()
			.map(it -> it.split(SPLITTING_REGEX))
			.collect(groupingBy(it -> it[0], mapping(it -> it[1].toUpperCase(), toList())));

}
