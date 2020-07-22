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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
public final class UserFilterConverter {

	private UserFilterConverter() {
		//static only
	}

	public static final Function<UserFilter, SharedEntity> TO_SHARED_ENTITY = filter -> {
		SharedEntity sharedEntity = SharedEntityConverter.TO_SHARED_ENTITY.apply(filter);
		sharedEntity.setName(filter.getName());
		sharedEntity.setDescription(filter.getDescription());
		return sharedEntity;
	};

	public static final Function<Set<UserFilter>, List<UserFilterResource>> FILTER_SET_TO_FILTER_RESOURCE = filters -> filters.stream()
			.map(UserFilterConverter::buildFilterResource)
			.collect(Collectors.toList());

	public static final Function<UserFilter, UserFilterResource> TO_FILTER_RESOURCE = UserFilterConverter::buildFilterResource;

	public static final Function<UserFilter, UserFilterActivityResource> TO_ACTIVITY_RESOURCE = filter -> {
		UserFilterActivityResource resource = new UserFilterActivityResource();
		resource.setId(filter.getId());
		resource.setName(filter.getName());
		resource.setDescription(filter.getDescription());
		resource.setProjectId(filter.getProject().getId());
		resource.setShared(filter.isShared());
		return resource;
	};

	private static final Function<FilterCondition, UserFilterCondition> TO_FILTER_CONDITION = filterCondition -> {
		UserFilterCondition condition = new UserFilterCondition();
		ofNullable(filterCondition.getCondition()).ifPresent(c -> {
			if (filterCondition.isNegative()) {
				condition.setCondition("!".concat(c.getMarker()));
			} else {
				condition.setCondition(c.getMarker());
			}
		});
		condition.setFilteringField(filterCondition.getSearchCriteria());
		condition.setValue(filterCondition.getValue());

		return condition;
	};

	private static final Function<FilterSort, Order> TO_FILTER_ORDER = filterSort -> {
		Order order = new Order();
		order.setSortingColumnName(filterSort.getField());
		order.setIsAsc(filterSort.getDirection().isAscending());
		return order;
	};

	private static UserFilterResource buildFilterResource(UserFilter filter) {
		UserFilterResource userFilterResource = new UserFilterResource();
		userFilterResource.setFilterId(filter.getId());
		userFilterResource.setName(filter.getName());
		userFilterResource.setDescription(filter.getDescription());
		userFilterResource.setShare(filter.isShared());
		userFilterResource.setOwner(filter.getOwner());
		ofNullable(filter.getTargetClass()).ifPresent(tc -> userFilterResource.setObjectType(tc.getClassObject().getSimpleName()));
		ofNullable(filter.getFilterCondition()).ifPresent(fcs -> userFilterResource.setConditions(fcs.stream()
				.map(UserFilterConverter.TO_FILTER_CONDITION)
				.collect(toSet())));
		ofNullable(filter.getFilterSorts()).ifPresent(fs -> userFilterResource.setOrders(fs.stream()
				.map(UserFilterConverter.TO_FILTER_ORDER)
				.collect(toList())));

		return userFilterResource;
	}
}
