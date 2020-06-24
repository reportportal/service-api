/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractStatisticsFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<Filter, Sort> buildFilter(Widget widget) {
		return buildFilterSortMap(widget, widget.getProject().getId());
	}

	protected Map<Filter, Sort> buildFilterSortMap(Widget widget, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();
		Set<UserFilter> userFilters = Optional.ofNullable(widget.getFilters()).orElse(Collections.emptySet());
		Filter defaultFilter = buildDefaultFilter(widget, projectId);
		Optional.ofNullable(defaultFilter).ifPresent(f -> filterSortMap.put(defaultFilter, Sort.unsorted()));
		userFilters.forEach(userFilter -> {
			Filter filter = new Filter(userFilter.getId(),
					userFilter.getTargetClass().getClassObject(),
					Lists.newArrayList(userFilter.getFilterCondition())
			);
			Optional<Set<FilterSort>> filterSorts = ofNullable(userFilter.getFilterSorts());
			Sort sort = Sort.by(filterSorts.map(filterSort -> filterSort.stream()
					.map(s -> Sort.Order.by(s.getField()).with(s.getDirection()))
					.collect(Collectors.toList())).orElseGet(Collections::emptyList));
			filterSortMap.put(filter, sort);
		});

		return filterSortMap;
	}

	protected abstract Filter buildDefaultFilter(Widget widget, Long projectId);
}
