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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.querygen.Condition;
import com.epam.ta.reportportal.store.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.store.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.store.database.entity.filter.FilterSort;
import com.epam.ta.reportportal.store.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateUserFilterHandlerImpl implements ICreateUserFilterHandler {

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Override
	public EntryCreatedRS createFilter(CreateUserFilterRQ createFilterRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		Project project = new Project();
		project.setId(projectDetails.getProjectId());

		UserFilter userFilter = new UserFilter();
		userFilter.setName(createFilterRQ.getName());
		userFilter.setProject(project);
		userFilter.setDescription(createFilterRQ.getDescription());
		userFilter.setTargetClass(createFilterRQ.getObjectType());

		List<FilterCondition> conditions = createFilterRQ.getEntities()
				.stream()
				.map(entity -> FilterCondition.builder()
						.withSearchCriteria(entity.getFilteringField())
						.withValue(entity.getValue())
						.withCondition(Condition.findByMarker(entity.getCondition())
								.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST, entity.getCondition())))
						.build())
				.collect(toList());

		createFilterRQ.getSelectionParameters().getOrders().forEach(order -> {
			FilterSort filterSort = new FilterSort();
			filterSort.setField(order.getSortingColumnName());
			filterSort.setAscending(order.getIsAsc());
			userFilter.getFilterSorts().add(filterSort);
		});

		userFilter.getFilterCondition().addAll(conditions);

		userFilterRepository.save(userFilter);

		return new EntryCreatedRS(userFilter.getId());
	}
}
