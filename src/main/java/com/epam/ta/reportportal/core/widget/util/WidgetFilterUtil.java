/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.util;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class WidgetFilterUtil {

	public static final Function<Set<Filter>, Filter> GROUP_FILTERS = filters -> {
		Filter filter = ofNullable(filters).map(Collection::stream)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Filters set should not be empty"))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "No filters for widget were found"));
		filters.stream().flatMap(f -> f.getFilterConditions().stream()).forEach(filter::withCondition);

		return filter;
	};

	public static final Function<Collection<Sort>, Sort> GROUP_SORTS = sorts -> Sort.by(ofNullable(sorts).map(s -> s.stream()
			.flatMap(sortStream -> Lists.newArrayList(sortStream.iterator()).stream())
			.collect(Collectors.toList())).orElseGet(Lists::newArrayList));

}
