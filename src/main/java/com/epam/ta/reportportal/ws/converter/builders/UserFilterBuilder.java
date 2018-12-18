/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
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
		userFilter.setTargetClass(ObjectType.getObjectTypeByName(rq.getObjectType()));
		ofNullable(rq.getShare()).ifPresent(it -> userFilter.setShared(it));
		addFilterConditions(rq.getConditions());
		addSelectionParameters(rq.getOrders());
		return this;
	}

	public UserFilterBuilder addUpdateFilterRQ(UpdateUserFilterRQ rq) {
		ofNullable(rq.getName()).ifPresent(it -> userFilter.setName(it));
		ofNullable(rq.getDescription()).ifPresent(it -> userFilter.setDescription(it));
		ofNullable(rq.getObjectType()).ifPresent(it -> userFilter.setTargetClass(ObjectType.getObjectTypeByName(rq.getObjectType())));
		ofNullable(rq.getShare()).ifPresent(it -> userFilter.setShared(it));
		addFilterConditions(rq.getConditions());
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
		userFilter.getFilterCondition().clear();
		ofNullable(conditions).ifPresent(c -> userFilter.getFilterCondition()
				.addAll(c.stream()
						.map(entity -> FilterCondition.builder()
								.withSearchCriteria(entity.getFilteringField())
								.withValue(entity.getValue())
								.withCondition(Condition.findByMarker(entity.getCondition())
										.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, entity.getCondition())))
								.build())
						.collect(toList())));

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
		userFilter.getFilterSorts().clear();
		ofNullable(orders).ifPresent(o -> o.forEach(order -> {
			FilterSort filterSort = new FilterSort();
			filterSort.setField(order.getSortingColumnName());
			filterSort.setDirection(order.getIsAsc() ? Sort.Direction.ASC : Sort.Direction.DESC);
			userFilter.getFilterSorts().add(filterSort);
		}));
		return this;
	}

	public UserFilterBuilder addProject(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		userFilter.setProject(project);
		return this;
	}

	public UserFilterBuilder addOwner(String owner) {
		userFilter.setOwner(owner);
		return this;
	}

	@Override
	public UserFilter get() {
		return userFilter;
	}
}
