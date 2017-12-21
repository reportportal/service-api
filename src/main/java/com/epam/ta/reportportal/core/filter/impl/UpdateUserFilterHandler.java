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

import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.acl.SharingService;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.filter.ObjectType;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.events.FilterUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of {@link IUpdateUserFilterHandler}
 *
 * @author Aliaksei_Makayed
 */
@Service
public class UpdateUserFilterHandler implements IUpdateUserFilterHandler {

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserFilterValidationService userFilterService;

	@Autowired
	private SharingService sharingService;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public OperationCompletionRS updateUserFilter(String userFilterId, UpdateUserFilterRQ updateRQ, String userName, String projectName,
			UserRole userRole) {

		UserFilter existingFilter = userFilterRepository.findOne(userFilterId);
		expect(existingFilter, notNull()).verify(USER_FILTER_NOT_FOUND, userFilterId, userName);
		AclUtils.isAllowedToEdit(existingFilter.getAcl(), userName, projectRepository.findProjectRoles(userName), existingFilter.getName(),
				userRole
		);
		expect(existingFilter.getProjectName(), equalTo(projectName)).verify(ACCESS_DENIED);
		UserFilter before = SerializationUtils.clone(existingFilter);

		// added synchronization because if user1 in one session checked that
		// filter name is unique
		// it should be unique until user1 save this filter
		// check than act situation
		synchronized (this) {
			if (null != updateRQ.getName() && !updateRQ.getName().equals(existingFilter.getName())) {
				userFilterService.isFilterNameUnique(userName, updateRQ.getName(), projectName);
			}
			updateUserFilter(existingFilter, updateRQ, userName, projectName);
			userFilterRepository.save(existingFilter);
			eventPublisher.publishEvent(new FilterUpdatedEvent(before, existingFilter, userName));
		}

		return buildResponse(existingFilter);
	}

	@Override
	public List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateFilterRQs, String userName,
			String projectName, UserRole userRole) {

		Set<String> idsToLoad = updateFilterRQs.getElements().stream().map(BulkUpdateFilterRQ::getId).collect(toSet());
		expect(idsToLoad.size(), equalTo(updateFilterRQs.getElements().size())).verify(BAD_REQUEST_ERROR);
		UserFilter[] userFilters = StreamSupport.stream(userFilterRepository.findAll(idsToLoad).spliterator(), false)
				.toArray(UserFilter[]::new);

		expect(idsToLoad.size(), equalTo(userFilters.length)).verify(USER_FILTER_NOT_FOUND);
		final List<UserFilter> filterFromOtherProjects = Stream.of(userFilters)
				.filter(userFilter -> !userFilter.getProjectName().equalsIgnoreCase(projectName))
				.collect(toList());
		expect(filterFromOtherProjects.size(), equalTo(0)).verify(ACCESS_DENIED);

		final Map<String, ProjectRole> projectRoles = projectRepository.findProjectRoles(userName);

		List<OperationCompletionRS> result = new ArrayList<>(idsToLoad.size());
		synchronized (this) {
			List<UserFilter> updatedFilters = new ArrayList<>(idsToLoad.size());
			for (int i = 0; i < updateFilterRQs.getElements().size(); i++) {
				AclUtils.isAllowedToEdit(userFilters[i].getAcl(), userName, projectRoles, userFilters[i].getName(), userRole);
				String name = updateFilterRQs.getElements().get(i).getName();
				if (null != name && !name.equals(userFilters[i].getName())) {
					userFilterService.isFilterNameUnique(userName, updateFilterRQs.getElements().get(i).getName(), projectName);
				}
				updateUserFilter(userFilters[i], updateFilterRQs.getElements().get(i), userName, projectName);
				updatedFilters.add(userFilters[i]);
				result.add(buildResponse(userFilters[i]));
			}
			userFilterRepository.save(updatedFilters);
		}
		return result;
	}

	private void updateUserFilter(UserFilter toUpdate, UpdateUserFilterRQ updateRQ, String userName, String projectName) {
		if (null != updateRQ.getName()) {
			toUpdate.setName(updateRQ.getName().trim());
		}
		if (null != updateRQ.getEntities()) {
			toUpdate.setFilter(createFilter(updateRQ.getObjectType(), updateRQ.getEntities()));
		}
		toUpdate.setDescription(updateRQ.getDescription());
		toUpdate.setIsLink(updateRQ.getIsLink());
		if (null != updateRQ.getSelectionParameters()) {
			updateRQ.getSelectionParameters()
					.getOrders()
					.stream()
					.map(Order::getSortingColumnName)
					.forEach(columnName -> userFilterService.validateSortingColumnName(toUpdate.getFilter().getTarget(), columnName));
			toUpdate.setSelectionOptions(UserFilterConverter.TO_SELECTION_OPTIONS.apply(updateRQ.getSelectionParameters()));
		}
		if (null != updateRQ.getShare()) {
			sharingService.modifySharing(Lists.newArrayList(toUpdate), userName, projectName, updateRQ.getShare());

		}
	}

	private Filter createFilter(String objectType, Set<UserFilterEntity> entities) {
		Set<FilterCondition> filterConditions = new LinkedHashSet<>(entities.size());
		for (UserFilterEntity filterEntity : entities) {
			Condition conditionObject = Condition.findByMarker(filterEntity.getCondition()).orElse(null);
			FilterCondition filterCondition = new FilterCondition(conditionObject, Condition.isNegative(filterEntity.getCondition()),
					filterEntity.getValue().trim(), filterEntity.getFilteringField().trim()
			);
			filterConditions.add(filterCondition);
		}
		return new Filter(ObjectType.getTypeByName(objectType), filterConditions);
	}

	private OperationCompletionRS buildResponse(UserFilter existingFilter) {
		return new OperationCompletionRS("User filter with ID = '" + existingFilter.getId() + "' successfully updated.");
	}
}
