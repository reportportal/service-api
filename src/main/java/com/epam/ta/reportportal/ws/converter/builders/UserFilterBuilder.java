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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.querygen.Condition;
import com.epam.ta.reportportal.store.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.store.database.entity.filter.FilterSort;
import com.epam.ta.reportportal.store.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
public class UserFilterBuilder implements Supplier<UserFilter> {

	private UserFilter userFilter;

	public UserFilterBuilder() {
		userFilter = new UserFilter();
	}

	public UserFilterBuilder(UserFilter userFilter) {
		this.userFilter = userFilter;
	}

	public UserFilterBuilder addCreateRq(CreateUserFilterRQ rq) {
		userFilter.setName(rq.getName());
		userFilter.setDescription(rq.getDescription());
		userFilter.setTargetClass(rq.getObjectType());
		addFilterConditions(rq.getEntities());
		addSelectionParameters(rq.getOrders());
		return this;
	}

	/**
	 * Convert provided conditions into db and add them to filter object
	 *
	 * @param conditions Conditions from rq
	 * @return UserFilterBuilder
	 */
	public UserFilterBuilder addFilterConditions(Set<UserFilterCondition> conditions) {
		userFilter.getFilterCondition()
				.addAll(conditions.stream()
						.map(entity -> FilterCondition.builder()
								.withSearchCriteria(entity.getFilteringField())
								.withValue(entity.getValue())
								.withCondition(Condition.findByMarker(entity.getCondition())
										.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, entity.getCondition())))
								.build())
						.collect(toList()));
		return this;
	}

	/**
	 * Convert provided selection into db and add them in correct order
	 * to filter object
	 *
	 * @param orders Filter sorting conditions
	 * @return UserFilterBuilder
	 */
	public UserFilterBuilder addSelectionParameters(List<Order> orders) {
		orders.forEach(order -> {
			FilterSort filterSort = new FilterSort();
			filterSort.setField(order.getSortingColumnName());
			filterSort.setAscending(order.getIsAsc());
			userFilter.getFilterSorts().add(filterSort);
		});
		return this;
	}

	public UserFilterBuilder addProject(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		userFilter.setProject(project);
		return this;
	}

	@Override
	public UserFilter get() {
		return userFilter;
	}
}
