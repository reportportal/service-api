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

package com.epam.ta.reportportal.core.widget.util;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Set;
import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public class WidgetFilterUtil {

	public static final Function<Set<Filter>, Filter> GROUP_FILTERS = filters -> {
		Filter filter = filters.stream()
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "No filters for widget"));
		filters.stream().skip(1).flatMap(f -> f.getFilterConditions().stream()).forEach(filter::withCondition);

		return filter;
	};

}
