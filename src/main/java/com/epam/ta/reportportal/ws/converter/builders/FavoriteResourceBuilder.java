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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;

/**
 * Builder for {@link FavoriteResource} persistence layer object.
 * 
 * @author Aliaksei_Makayed
 * 
 */

@Service
@Scope("prototype")
public class FavoriteResourceBuilder extends Builder<FavoriteResource>  {

	@Override
	protected FavoriteResource initObject() {
		return new FavoriteResource();
	}
	
	public FavoriteResourceBuilder addUser(String userName) {
		getObject().setUserName(userName);
		return this;
	}
	
	public FavoriteResourceBuilder addProject(String projectName) {
		getObject().setProjectName(projectName);
		return this;
	}
	
	public FavoriteResourceBuilder addCreateRQ(AddFavoriteResourceRQ addFavoriteResourceRQ) {
		if (addFavoriteResourceRQ != null) {
			getObject().setResourceId(addFavoriteResourceRQ.getResourceId());
			getObject().setResourceType(addFavoriteResourceRQ.getType().name());
		}
		return this;
	}

}