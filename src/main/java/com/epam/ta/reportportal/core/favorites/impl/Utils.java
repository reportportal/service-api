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
 
package com.epam.ta.reportportal.core.favorites.impl;

import java.util.Set;

import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.google.common.collect.Sets;

/**
 * Provide utilities for working with favorite resources.
 * 
 * @author Aliaksei_Makayed
 * 
 */
public class Utils {

	private Utils() {
	}
	
	public static Filter getUniqueFavoriteFilter(String userName, String type, String resourceId) {
		Set<FilterCondition> conditions = Sets.newHashSet(
				new FilterCondition(Condition.EQUALS, false, userName, FavoriteResource.USERNAME_CRITERIA),
				new FilterCondition(Condition.EQUALS, false, type, FavoriteResource.TYPE_CRITERIA),
				new FilterCondition(Condition.EQUALS, false, resourceId, FavoriteResource.RESOURCE_ID_CRITERIA));
		return new Filter(FavoriteResource.class, conditions);
	}
}