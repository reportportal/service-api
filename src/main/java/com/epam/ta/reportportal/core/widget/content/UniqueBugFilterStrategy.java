/*
 * Copyright 2016 EPAM Systems
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

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.SelectionOrder;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.*;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of
 * {@link com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy} for
 * unique bug widget
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class UniqueBugFilterStrategy implements BuildFilterStrategy {

	private static final String SEPARATOR = ",";

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private WidgetContentProvider widgetContentProvider;

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {
		Filter filter = userFilter.getFilter();
		if (filter.getTarget().equals(Launch.class)) {
			filter.addCondition(new FilterCondition(Condition.EQUALS, false, projectName, Launch.PROJECT));
			filter.addCondition(FilterConditionUtils.LAUNCH_IN_DEFAULT_MODE());
			filter.addCondition(FilterConditionUtils.LAUNCH_NOT_IN_PROGRESS());
			int limit = contentOptions.getItemsCount();

			CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(filter.getTarget());
			List<Sort.Order> orders = userFilter.getSelectionOptions()
					.getOrders()
					.stream()
					.map(order -> new Sort.Order(order.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
							criteriaMap.getCriteriaHolder(order.getSortingColumnName()).getQueryCriteria()
					))
					.collect(toList());
			Sort sort = new Sort(orders);

			List<Launch> launches = launchRepository.findIdsByFilter(filter, sort, limit);
			final String value = launches.stream().map(Launch::getId).collect(Collectors.joining(SEPARATOR));
			filter = new Filter(TestItem.class, Sets.newHashSet(new FilterCondition(Condition.IN, false, value, TestItem.LAUNCH_CRITERIA)));
		}
		filter.addCondition(new FilterCondition(Condition.EXISTS, false, "true", TestItem.EXTERNAL_SYSTEM_ISSUES));
		SelectionOptions selectionOptions = new SelectionOptions();
		selectionOptions.setOrders(Lists.newArrayList(new SelectionOrder("start_time", false)));

		return widgetContentProvider.getChartContent(projectName, filter, selectionOptions, contentOptions);
	}

}