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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.filter.ObjectType;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.SelectionParameters;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builder for {@link UserFilter}
 *
 * @author Aliaksei_Makayed
 */
@Service
@Scope("prototype")
public class UserFilterBuilder extends ShareableEntityBuilder<UserFilter> {

	public UserFilterBuilder addCreateRQ(CreateUserFilterRQ request) {
		if (request != null) {
			getObject().setName(request.getName().trim());
			getObject().setIsLink(request.getIsLink());
			Set<UserFilterEntity> filterEntities = request.getEntities();
			getObject().setFilter(getFilter(filterEntities, request.getObjectType()));

			addSelectionParamaters(request.getSelectionParameters());
		}
		return this;
	}

	public UserFilterBuilder addSelectionParamaters(SelectionParameters parameters) {
		if (parameters != null) {
			getObject().setSelectionOptions(UserFilterConverter.TO_SELECTION_OPTIONS.apply(parameters));
		}
		return this;
	}

	@Override
	public UserFilterBuilder addSharing(String owner, String project, String description, boolean isShare) {
		super.addAcl(owner, project, description, isShare);
		return this;
	}

	public UserFilterBuilder addProject(String projectName) {
		getObject().setProjectName(projectName);
		return this;
	}

	@Override
	protected UserFilter initObject() {
		return new UserFilter();
	}

	/**
	 * Convert Set<{@link UserFilterEntity}> to {@link Filter} object
	 *
	 * @param filterEntities
	 * @return
	 */
	private Filter getFilter(Set<UserFilterEntity> filterEntities, String objectType) {
		if (filterEntities == null) {
			return null;
		}

		Set<FilterCondition> filterConditions = new LinkedHashSet<>(filterEntities.size());
		for (UserFilterEntity filterEntity : filterEntities) {
			Condition conditionObject = Condition.findByMarker(filterEntity.getCondition()).orElse(null);
			FilterCondition filterCondition = new FilterCondition(conditionObject, Condition.isNegative(filterEntity.getCondition()),
					filterEntity.getValue().trim(), filterEntity.getFilteringField().trim()
			);
			filterConditions.add(filterCondition);
		}
		return new Filter(ObjectType.getTypeByName(objectType), filterConditions);
	}

}