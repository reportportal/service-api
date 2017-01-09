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
 
package com.epam.ta.reportportal.ws.controller.impl;

import java.security.Principal;

import com.epam.ta.reportportal.commons.EntityUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.epam.ta.reportportal.core.favorites.IAddToFavoritesHandler;
import com.epam.ta.reportportal.core.favorites.IRemoveFromFavoritesHandler;
import com.epam.ta.reportportal.ws.controller.IFavoriteResourceController;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;

/**
 * Default implementation of {@link IFavoriteResourceController}
 * 
 * @author Aliaksei_Makayed
 * 
 */

@Controller
@RequestMapping("/{projectName}/favorites")
public class FavoriteResourceController implements IFavoriteResourceController {

	@Autowired
	private IAddToFavoritesHandler addToFavoritesHandler;

	@Autowired
	private IRemoveFromFavoritesHandler removeFromFavoritesHandler;

	@Override
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@ApiOperation("Add resource to favorites")
	public DashboardResource addFavoriteResource(@RequestBody @Validated AddFavoriteResourceRQ addFavoriteResourceRQ,
			Principal principal, @PathVariable String projectName) {
		return addToFavoritesHandler.add(addFavoriteResourceRQ, principal.getName(), EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Remove resource from favorites")
	public OperationCompletionRS removeFromFavorites(Principal principal, @RequestParam(value = "resource_id") String resourceId,
			@RequestParam(value = "resource_type") String resourceType) {
		return removeFromFavoritesHandler.remove(resourceType, resourceId, principal.getName());
	}

}