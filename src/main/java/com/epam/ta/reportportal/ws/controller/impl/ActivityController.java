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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.IActivityController;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Dzmitry_Kavalets
 * @author Andrei Varabyeu
 */
@Controller
@RequestMapping("/{projectName}/activity")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ActivityController implements IActivityController {

	@Autowired
	private IActivityHandler activityHandler;

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get activities for project")
	public Page<ActivityResource> getActivities(@PathVariable String projectName, @FilterFor(Activity.class) Filter filter,
			@SortFor(Activity.class) Pageable pageable) {
		return activityHandler.getItemActivities(EntityUtils.normalizeId(projectName), filter, pageable);
	}

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
	@ApiOperation("Get activities for test item")
	public List<ActivityResource> getTestItemActivities(@PathVariable String projectName, @PathVariable String itemId,
			@FilterFor(Activity.class) Filter filter, @SortFor(Activity.class) Pageable pageable, Principal principal) {
		return activityHandler.getItemActivities(EntityUtils.normalizeId(projectName), itemId, filter, pageable);
	}
}
