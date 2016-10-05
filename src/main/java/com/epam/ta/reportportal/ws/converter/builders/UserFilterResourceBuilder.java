/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.filter.SelectionParameters;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;

/**
 * Builder for {@link UserFilterResource} domain object.
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
@Scope("prototype")
public class UserFilterResourceBuilder extends ResourceBuilder<UserFilterResource> {

	@Override
	protected UserFilterResource initObject() {
		return new UserFilterResource();
	}

	public UserFilterResourceBuilder addUserFilter(UserFilter userFilter) {
		if (userFilter != null) {
			getObject().setFilterId(userFilter.getId());
			getObject().setName(userFilter.getName());
			getObject().setDescription(userFilter.getDescription());
			Filter filter = userFilter.getFilter();
			if (filter != null) {
				getObject().setObjectType(filter.getTarget().getSimpleName().toLowerCase());
				getObject().setEntities(transformFilterEntities(filter));
			}
			if (null != userFilter.getSelectionOptions()) {
				SelectionParameters selectionParameters = new SelectionParameters();
				selectionParameters.setQuantity(userFilter.getSelectionOptions().getQuantity());
				selectionParameters.setSortingColumnName(userFilter.getSelectionOptions().getSortingColumnName());
				selectionParameters.setIsAsc(userFilter.getSelectionOptions().isAsc());
				selectionParameters.setPageNumber(userFilter.getSelectionOptions().getPageNumber());
				getObject().setSelectionParameters(selectionParameters);
			}
			if (null != userFilter.getAcl()) {
				getObject().setOwner(userFilter.getAcl().getOwnerUserId());
				getObject().setIsShared(!userFilter.getAcl().getEntries().isEmpty());
			}
		}
		return this;
	}

	/**
	 * Transform Set<{@link Filter}> to Set<{@link UserFilterEntity}>
	 * 
	 * @param filterEntities
	 * @return Set<ComplexFilterEntity>
	 */
	private Set<UserFilterEntity> transformFilterEntities(Filter filterEntities) {
		Set<UserFilterEntity> result = new LinkedHashSet<>();
		UserFilterEntity userFilterEntity;
		for (FilterCondition filterCondition : filterEntities.getFilterConditions()) {
			userFilterEntity = new UserFilterEntity();
			if (filterCondition.getCondition() != null) {
				userFilterEntity.setCondition(filterCondition.getCondition().getMarker());
			}
			userFilterEntity.setIsNegative(filterCondition.isNegative());
			userFilterEntity.setValue(filterCondition.getValue());
			userFilterEntity.setFilteringField(filterCondition.getSearchCriteria());
			result.add(userFilterEntity);
		}
		return result;
	}

}