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

import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.SelectionOrder;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.SelectionParameters;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class UserFilterConverter {

	private UserFilterConverter() {
		//static only
	}

	public static final Function<UserFilter, UserFilterResource> TO_RESOURCE = filter -> {
		Preconditions.checkNotNull(filter);
		UserFilterResource resource = new UserFilterResource();
		resource.setFilterId(filter.getId());
		resource.setName(filter.getName());
		resource.setDescription(filter.getDescription());
		Optional.ofNullable(filter.getFilter()).ifPresent(f -> {
			resource.setObjectType(f.getTarget().getSimpleName().toLowerCase());
			resource.setEntities(UserFilterConverter.TO_ENTITIES.apply(f));
		});
		Optional.ofNullable(filter.getSelectionOptions())
				.ifPresent(it -> resource.setSelectionParameters(UserFilterConverter.TO_SELECTION_PARAMETERS.apply(it)));
		Optional.ofNullable(filter.getAcl()).ifPresent(acl -> {
			resource.setOwner(acl.getOwnerUserId());
			resource.setShare(!acl.getEntries().isEmpty());
		});
		return resource;
	};

	public static final Function<SelectionParameters, SelectionOptions> TO_SELECTION_OPTIONS = parameters -> {
		Preconditions.checkNotNull(parameters);
		Preconditions.checkNotNull(parameters.getOrders());
		SelectionOptions selectionOptions = new SelectionOptions();
		selectionOptions.setPageNumber(parameters.getPageNumber());
		selectionOptions.setOrders(parameters.getOrders().stream().map(order -> {
			SelectionOrder selectionOrder = new SelectionOrder();
			selectionOrder.setIsAsc(order.getIsAsc());
			selectionOrder.setSortingColumnName(order.getSortingColumnName());
			return selectionOrder;
		}).collect(Collectors.toList()));
		return selectionOptions;
	};

	public static final Function<SelectionOptions, SelectionParameters> TO_SELECTION_PARAMETERS = options -> {
		Preconditions.checkNotNull(options);
		Preconditions.checkNotNull(options.getOrders());
		SelectionParameters selectionParameters = new SelectionParameters();
		selectionParameters.setPageNumber(options.getPageNumber());
		selectionParameters.setOrders(options.getOrders().stream().map(selectionOrder -> {
			Order order = new Order();
			order.setIsAsc(selectionOrder.isAsc());
			order.setSortingColumnName(selectionOrder.getSortingColumnName());
			return order;
		}).collect(Collectors.toList()));
		return selectionParameters;
	};

	/**
	 * Transform Set<{@link Filter}> to Set<{@link UserFilterEntity}>
	 *
	 * @param filterEntities
	 * @return Set<ComplexFilterEntity>
	 */
	private static final Function<Filter, Set<UserFilterEntity>> TO_ENTITIES = filter -> {
		Set<UserFilterEntity> result = Sets.newLinkedHashSet();
		filter.getFilterConditions().forEach(condition -> {
			UserFilterEntity userFilterEntity = new UserFilterEntity();

			Optional.ofNullable(condition.getCondition().getMarker())
					.map(it -> Condition.makeNegative(condition.isNegative(), it))
					.ifPresent(userFilterEntity::setCondition);

			userFilterEntity.setValue(condition.getValue());
			userFilterEntity.setFilteringField(condition.getSearchCriteria());
			result.add(userFilterEntity);
		});
		return result;
	};

}
