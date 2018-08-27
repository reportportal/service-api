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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;

import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
public final class UserFilterConverter {

	private UserFilterConverter() {
		//static only
	}

	public static final Function<UserFilter, UserFilterResource> TO_FILTER_RESOURCE = filter -> {
		UserFilterResource userFilterResource = new UserFilterResource();
		userFilterResource.setFilterId(filter.getId());
		userFilterResource.setName(filter.getName());
		userFilterResource.setDescription(filter.getDescription());
		userFilterResource.setObjectType(filter.getTargetClass().getSimpleName());
		userFilterResource.setConditions(filter.getFilterCondition()
				.stream()
				.map(UserFilterConverter.TO_FILTER_CONDITION)
				.collect(toSet()));
		userFilterResource.setOrders(filter.getFilterSorts().stream().map(UserFilterConverter.TO_FILTER_ORDER).collect(toList()));
		return userFilterResource;
	};

	private static final Function<FilterCondition, UserFilterCondition> TO_FILTER_CONDITION = filterCondition -> {
		UserFilterCondition condition = new UserFilterCondition();
		condition.setCondition(filterCondition.getCondition().getMarker());
		condition.setFilteringField(filterCondition.getSearchCriteria());
		condition.setValue(filterCondition.getValue());
		return condition;
	};

	private static final Function<FilterSort, Order> TO_FILTER_ORDER = filterSort -> {
		Order order = new Order();
		order.setSortingColumnName(filterSort.getField());
		//TODO CHECK
		order.setIsAsc(filterSort.getDirection().isAscending());
		//order.setIsAsc(filterSort.isAscending());
		return order;
	};
}
