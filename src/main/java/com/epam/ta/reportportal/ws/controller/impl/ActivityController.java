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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import java.util.List;

import com.epam.ta.reportportal.commons.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.IActivityController;
import com.epam.ta.reportportal.ws.model.ActivityResource;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Dzmitry_Kavalets
 */
@Controller
@RequestMapping("/{projectName}/activity")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ActivityController implements IActivityController {

	@Autowired
	private IActivityHandler activityHandler;

	@Override
	@RequestMapping(value = "/{activityId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	public ActivityResource getActivity(@PathVariable String projectName, @PathVariable String activityId, Principal principal) {
		return activityHandler.getActivity(EntityUtils.normalizeId(projectName), activityId);
	}

	@Override
	@RequestMapping(value = "/item/{itemId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get activities by filter")
	public List<ActivityResource> getTestItemActivities(@PathVariable String projectName, @PathVariable String itemId,
			@FilterFor(Activity.class) Filter filter, @SortFor(Activity.class) Pageable pageable, Principal principal) {
		return activityHandler.getItemActivities(EntityUtils.normalizeId(projectName), itemId, filter, pageable);
	}
}
