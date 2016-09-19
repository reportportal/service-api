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

package com.epam.ta.reportportal.core.favorites.impl;

import java.util.List;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.favorites.IAddToFavoritesHandler;
import com.epam.ta.reportportal.core.favorites.IRemoveFromFavoritesHandler;
import com.epam.ta.reportportal.database.dao.FavoriteResourceRepository;
import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Default implementation of {@link IAddToFavoritesHandler}
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
public class RemoveFromFavoritesHandler implements IRemoveFromFavoritesHandler {

	@Autowired
	private FavoriteResourceRepository favoriteResourceRepository;

	@Override
	public OperationCompletionRS remove(String resourceType, String resourceId, String userName) {
		Filter filter = Utils.getUniqueFavoriteFilter(userName, resourceType, resourceId);
		List<FavoriteResource> resources = favoriteResourceRepository.findByFilter(filter);
		BusinessRule.expect(resources, Preconditions.NOT_EMPTY_COLLECTION).verify(ErrorType.UNABLE_REMOVE_FROM_FAVORITE,
				Suppliers.formattedSupplier("Favorite resource with resource id '{}', type '{}' haven't found for user '{}'.", resourceId,
						resourceType, userName));
		favoriteResourceRepository.delete(resources.get(0).getId());
		return new OperationCompletionRS("Resource with ID = '" + resourceId + "' removed from favorites.");
	}
}