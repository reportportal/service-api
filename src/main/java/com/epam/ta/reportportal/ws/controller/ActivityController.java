/*
 *
 *  * Copyright 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Ihar_Kahadouski
 */
@Controller
@RequestMapping("/{projectName}/activity")
@Transactional(readOnly = true)
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ActivityController {

	private final IActivityHandler activityHandler;

	@Autowired
	public ActivityController(IActivityHandler activityHandler) {
		this.activityHandler = activityHandler;
	}

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get activities for project")
	public Page<ActivityResource> getActivities(@PathVariable String projectName, @FilterFor(Activity.class) Filter filter,
			@SortFor(Activity.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectName));
		return activityHandler.getItemActivities(projectDetails, filter, pageable);
	}

	@RequestMapping(value = "/{activityId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	public ActivityResource getActivity(@PathVariable String projectName, @PathVariable Long activityId,
			@AuthenticationPrincipal ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectName));
		return activityHandler.getActivity(projectDetails, activityId);
	}

	@RequestMapping(value = "/item/{itemId}", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Get activities for test item")
	public Iterable<ActivityResource> getTestItemActivities(@PathVariable String projectName, @PathVariable Long itemId,
			@FilterFor(Activity.class) Filter filter, @SortFor(Activity.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, EntityUtils.normalizeId(projectName));
		return activityHandler.getItemActivities(projectDetails, itemId, filter, pageable);
	}
}