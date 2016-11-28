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

package com.epam.ta.reportportal.core.filter.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.filter.ObjectType;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;

/**
 * Default implementation of {@link ICreateUserFilterHandler}
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
public class CreateUserFilterHandler implements ICreateUserFilterHandler {

	@Autowired
	private UserFilterRepository filterRepository;

	@Autowired
	@Qualifier("userFilterBuilder.reference")
	private LazyReference<UserFilterBuilder> userFilterBuilder;

	@Autowired
	private UserFilterValidationService userFilterService;

	@Override
	public List<EntryCreatedRS> createFilter(String userName, String projectName, CollectionsRQ<CreateUserFilterRQ> createFilterRQ) {

		List<UserFilter> filters = new ArrayList<>();

		// validate request
		for (CreateUserFilterRQ rq : createFilterRQ.getElements()) {

			Set<UserFilterEntity> updatedEntries = userFilterService
					.validateUserFilterEntities(ObjectType.getTypeByName(rq.getObjectType()), rq.getEntities());

			/*
			 * If Entries contains new statistic model, update it avoid
			 * validation conflicts
			 */
			rq.setEntities(updatedEntries);

			/* Build user filter entity */
			UserFilter userFilter = userFilterBuilder.get().addCreateRQ(rq).addProject(projectName)
					.addSharing(userName, projectName, rq.getShare() == null ? false : rq.getShare()).build();

			userFilterService.validateSortingColumnName(userFilter.getFilter().getTarget(),
					userFilter.getSelectionOptions().getSortingColumnName());
			filters.add(userFilter);
		}
		// temporary removed - reason memory problems with compound indexes
		// by 3
		// added synchronization because if user1 in one session checked
		// that
		// filter name is unique
		// it should be unique until user1 save this filter
		// check than act situation
		Set<String> filterNames = new HashSet<>();

		synchronized (this) {
			for (UserFilter userFilter : filters) {
				userFilterService.isFilterNameUnique(userName, userFilter.getName().trim(), projectName);
				filterNames.add(userFilter.getName());
			}
			BusinessRule.expect(filterNames.size(), Predicates.equalTo(filters.size())).verify(ErrorType.BAD_SAVE_USER_FILTER_REQUEST);
			filterRepository.save(filters);
		}
		return filters.stream().map(filter -> new EntryCreatedRS(filter.getId())).collect(Collectors.toList());
	}
}