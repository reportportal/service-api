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

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service("productStatusFilterStrategy")
public class ProductStatusFilterStrategy extends AbstractStatisticsFilterStrategy {

	@Override
	protected Map<Filter, Sort> buildFilterSortMap(Widget widget, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();
		Optional.ofNullable(widget.getFilters()).orElse(Collections.emptySet()).forEach(userFilter -> {
			Filter filter = new Filter(
					userFilter.getId(),
					userFilter.getTargetClass().getClassObject(),
					Lists.newArrayList(userFilter.getFilterCondition())
			);
			filter.withConditions(buildDefaultFilter(widget, projectId).getFilterConditions());

			Optional<Set<FilterSort>> filterSorts = ofNullable(userFilter.getFilterSorts());

			Sort sort = Sort.by(filterSorts.map(filterSort -> filterSort.stream()
					.map(s -> Sort.Order.by(s.getField()).with(s.getDirection()))
					.collect(Collectors.toList())).orElseGet(Collections::emptyList));

			filterSortMap.put(filter, sort);
		});
		return filterSortMap;
	}

	protected Filter buildDefaultFilter(Widget widget, Long projectId) {
		return new Filter(
				Launch.class,
				Lists.newArrayList(new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID))
		);
	}
}
